<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Password Reset Verification Code</title>
</head>
<body>
    <h2>Password Reset Verification</h2>
    
    @if(!empty($userName))
        <p>Hello {{ $userName }},</p>
    @else
        <p>Hello,</p>
    @endif
    
    <p>You have requested to reset your password. Please use the following verification code:</p>
    
    <h3>{{ $code }}</h3>
    
    <p>This code will expire in 10 minutes. If you did not request this, please ignore this email.</p>
    
    <p>Thank you,<br>
    ServiceHub Team</p>
</body>
</html>