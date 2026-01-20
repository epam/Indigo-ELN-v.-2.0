def lambda_handler(event, context):
    headers = event.get('headers', {})  # Get headers from the event

    s = "event:\n\n" + repr(event) + "\n\n"
    s += "context:\n\n" + repr(context) + "\n\n"
    for key, value in headers.items():
        s += f"{key}: {value}\n"

    return {
        'statusCode': 200,
        'body': s
    }
