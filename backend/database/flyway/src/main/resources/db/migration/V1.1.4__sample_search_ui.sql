ALTER TABLE Compound ADD COLUMN picture BYTEA;
-- TODO delete this after DB is recreated
UPDATE Compound
SET picture = '<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="500" height="200" viewBox="0 0 500 200">
<defs>
<g>
<g id="glyph-0-0">
<path d="M 0.796875 2.8125 L 0.796875 -11.25 L 8.765625 -11.25 L 8.765625 2.8125 Z M 1.6875 1.9375 L 7.890625 1.9375 L 7.890625 -10.359375 L 1.6875 -10.359375 Z M 1.6875 1.9375 "/>
</g>
<g id="glyph-0-1">
<path d="M 6.28125 -10.5625 C 5.144531 -10.5625 4.238281 -10.132812 3.5625 -9.28125 C 2.894531 -8.425781 2.5625 -7.265625 2.5625 -5.796875 C 2.5625 -4.335938 2.894531 -3.179688 3.5625 -2.328125 C 4.238281 -1.472656 5.144531 -1.046875 6.28125 -1.046875 C 7.425781 -1.046875 8.332031 -1.472656 9 -2.328125 C 9.664062 -3.179688 10 -4.335938 10 -5.796875 C 10 -7.265625 9.664062 -8.425781 9 -9.28125 C 8.332031 -10.132812 7.425781 -10.5625 6.28125 -10.5625 Z M 6.28125 -11.84375 C 7.914062 -11.84375 9.21875 -11.296875 10.1875 -10.203125 C 11.164062 -9.109375 11.65625 -7.640625 11.65625 -5.796875 C 11.65625 -3.960938 11.164062 -2.5 10.1875 -1.40625 C 9.21875 -0.320312 7.914062 0.21875 6.28125 0.21875 C 4.644531 0.21875 3.335938 -0.320312 2.359375 -1.40625 C 1.378906 -2.5 0.890625 -3.960938 0.890625 -5.796875 C 0.890625 -7.640625 1.378906 -9.109375 2.359375 -10.203125 C 3.335938 -11.296875 4.644531 -11.84375 6.28125 -11.84375 Z M 6.28125 -11.84375 "/>
</g>
<g id="glyph-0-2">
<path d="M 1.5625 -11.625 L 3.140625 -11.625 L 3.140625 -6.859375 L 8.859375 -6.859375 L 8.859375 -11.625 L 10.4375 -11.625 L 10.4375 0 L 8.859375 0 L 8.859375 -5.53125 L 3.140625 -5.53125 L 3.140625 0 L 1.5625 0 Z M 1.5625 -11.625 "/>
</g>
</g>
</defs>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 0.857688 0.995206 L 1.740305 1.504819 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 1.731981 1.50002 L 1.731981 2.499955 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 1.565309 1.596185 L 1.565309 2.40379 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 1.740305 2.495156 L 0.857688 3.004769 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 0.874335 3.004769 L -0.00838036 2.495156 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 0.874335 2.812342 L 0.158292 2.398992 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 0.0000413779 2.509649 L 0.0000413779 1.490423 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M -0.00838036 1.504819 L 0.874335 0.995206 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 0.158292 1.601081 L 0.874335 1.187633 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 1.723658 1.504819 L 2.606373 0.995206 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 2.589628 0.995206 L 3.188354 1.340791 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 2.681287 1.048086 L 2.681287 0.261241 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 2.514615 1.048086 L 2.514615 0.261241 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 1.723658 2.495156 L 2.322286 2.840839 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 2.597951 3.26124 L 2.597951 4.006858 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 2.597951 3.903741 L 1.966027 4.268715 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 2.65896 4.061012 L 2.049363 4.41306 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<path fill="none" stroke-width="0.0333333" stroke-linecap="butt" stroke-linejoin="miter" stroke="rgb(0%, 0%, 0%)" stroke-opacity="1" stroke-miterlimit="10" d="M 2.593153 3.995107 L 3.305084 4.707233 " transform="matrix(39.889336, 0, 0, 39.889336, 172.588193, 9.762846)"/>
<g fill="rgb(100%, 5.1%, 5.1%)" fill-opacity="1">
<use xlink:href="#glyph-0-1" x="304.488281" y="75.40625"/>
</g>
<g fill="rgb(100%, 5.1%, 5.1%)" fill-opacity="1">
<use xlink:href="#glyph-0-2" x="315.917969" y="75.195312"/>
</g>
<g fill="rgb(100%, 5.1%, 5.1%)" fill-opacity="1">
<use xlink:href="#glyph-0-1" x="269.941406" y="15.570312"/>
</g>
<g fill="rgb(100%, 5.1%, 5.1%)" fill-opacity="1">
<use xlink:href="#glyph-0-1" x="269.941406" y="135.238281"/>
</g>
<g fill="rgb(100%, 5.1%, 5.1%)" fill-opacity="1">
<use xlink:href="#glyph-0-1" x="235.394531" y="195.078125"/>
</g>
</svg>
'::BYTEA
WHERE true;
ALTER TABLE Compound ALTER COLUMN picture SET NOT NULL;
