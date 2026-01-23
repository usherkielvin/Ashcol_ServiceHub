<?php

namespace App\Mail;

use Illuminate\Bus\Queueable;
use Illuminate\Mail\Mailable;
use Illuminate\Queue\SerializesModels;

class VerificationCode extends Mailable
{
    use Queueable, SerializesModels;

    public $verificationCode;
    public $userName;

    /**
     * Create a new message instance.
     *
     * @param  string  $verificationCode
     * @param  string|null  $userName
     * @return void
     */
    public function __construct($verificationCode, $userName = null)
    {
        $this->verificationCode = $verificationCode;
        $this->userName = $userName;
    }

    /**
     * Build the message.
     *
     * @return $this
     */
    public function build()
    {
        $subject = 'Password Reset Verification Code';
        
        return $this->subject($subject)
                    ->view('emails.verification-code')
                    ->with([
                        'code' => $this->verificationCode,
                        'userName' => $this->userName,
                    ]);
    }
}