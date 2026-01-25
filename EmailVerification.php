<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class EmailVerification extends Model
{
    protected $fillable = [
        'email',
        'code',
        'expires_at',
        'verified'
    ];

    protected $casts = [
        'expires_at' => 'datetime',
        'verified' => 'boolean',
    ];

    /**
     * Generate a random verification code
     */
    public static function generateCode()
    {
        return strtoupper(substr(md5(random_bytes(16)), 0, 6));
    }

    /**
     * Check if the verification code has expired
     */
    public function isExpired()
    {
        return $this->expires_at && now()->greaterThan($this->expires_at);
    }

    /**
     * Scope to get unexpired codes
     */
    public function scopeUnexpired($query)
    {
        return $query->where('expires_at', '>', now());
    }
}