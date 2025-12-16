set MAVEN_HOME=C:\Soft\maven
set PATH=%PATH%;%MAVEN_HOME%\bin

set AWS_PROFILE=epam-lsop
rem aws sts get-caller-identity
if %ERRORLEVEL% neq 0 (
    rem aws sso login
    rem aws sts assume-role --role-arn "arn:aws:iam::657609648574:role/AdminUserSSO" --role-session-name "cdk-deploy"
    rem aws sts get-caller-identity || exit /b %ERRORLEVEL%
)
rem aws sts assume-role --role-arn "arn:aws:iam::657609648574:role/AdminUserSSO" --role-session-name "cdk-deploy"

set AWS_PROFILE=epam-lsop-admin
call cdk bootstrap --custom-permissions-boundary "eo_role_boundary" --no-execute --template cdk-bootstrap.yaml
