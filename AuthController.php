<?php

namespace App\Http\Controllers;

use App\Http\Controllers\Controller;
use App\Models\EmailVerification;
use App\Models\User;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Mail;

class AuthController extends Controller
{
    /**
     * Handle forgot password request
     * Implements secure email enumeration prevention
     */
    public function forgotPassword(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
        ]);

        // Check if user exists (but don't reveal this in the response)
        $user = User::where('email', $request->email)->first();
        
        // Always return success message to prevent email enumeration
        // Even if user doesn't exist, we return the same response
        
        // Generate verification code
        $verificationCode = EmailVerification::generateCode();
        
        // Store verification code (expires in 10 minutes)
        EmailVerification::updateOrCreate(
            ['email' => $request->email],
            [
                'code' => $verificationCode,
                'expires_at' => now()->addMinutes(10),
                'verified' => false,
            ]
        );

        // Send verification email if user exists
        if ($user) {
            try {
                Mail::to($request->email)->send(new \App\Mail\VerificationCode($verificationCode, $user->name ?? null));
            } catch (\Exception $e) {
                \Log::error('Failed to send password reset email: ' . $e->getMessage());
            }
        }
        
        return response()->json([
            'success' => true,
            'message' => 'If an account exists, a reset code has been sent to your email',
        ]);
    }

    /**
     * Verify the password reset code
     */
    public function verifyResetCode(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'code' => 'required|string',
        ]);

        $verification = EmailVerification::where('email', $request->email)
            ->where('code', $request->code)
            ->first();

        if (!$verification || $verification->isExpired() || $verification->verified) {
            return response()->json([
                'success' => false,
                'message' => 'Invalid or expired verification code',
            ], 400);
        }

        // Mark as verified to prevent reuse
        $verification->update(['verified' => true]);

        return response()->json([
            'success' => true,
            'message' => 'Verification code is valid',
        ]);
    }

    /**
     * Reset the user's password after verification
     */
    public function resetPassword(Request $request)
    {
        $request->validate([
            'email' => 'required|email',
            'code' => 'required|string',
            'password' => 'required|string|min:8|confirmed',
        ]);

        $verification = EmailVerification::where('email', $request->email)
            ->where('code', $request->code)
            ->first();

        if (!$verification || $verification->isExpired() || $verification->verified) {
            return response()->json([
                'success' => false,
                'message' => 'Invalid or expired verification code',
            ], 400);
        }

        $user = User::where('email', $request->email)->first();

        if (!$user) {
            return response()->json([
                'success' => false,
                'message' => 'User not found',
            ], 400);
        }

        // Update user password
        $user->password = bcrypt($request->password);
        $user->save();

        // Mark verification as used
        $verification->update(['verified' => true]);

        return response()->json([
            'success' => true,
            'message' => 'Password has been reset successfully',
        ]);
    }
}