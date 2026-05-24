# Event Macha Domain APIs

Production-ready **Quarkus 3 + Java 21** backend for the Event Macha platform.
Deployed as an **AWS Lambda ZIP** behind **AWS API Gateway (HTTP API)**.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21, Quarkus 3.9 |
| Deployment | AWS Lambda (ZIP) |
| API | AWS API Gateway HTTP API |
| Database | DynamoDB (Enhanced Client) |
| Auth | Amazon Cognito (JWT) |
| Payments | Razorpay |
| Email | Amazon SES |
| Build | Maven |

---

## Project Structure

```
src/main/java/com/eventmacha/
├── config/         – TableNamesConfig, AppConfig, JacksonConfig
├── controller/     – AuthController, OrderController, PaymentController,
│                     PlanController, PublishController, WebhookController
├── service/        – AuthService, OrderService, PaymentService,
│                     PlanService, PublishService
├── repository/     – UserRepository, RateCardRepository, OrderRepository,
│                     PaymentRepository, PaymentHistoryRepository, PublishRepository
├── entity/         – DynamoDB @DynamoDbBean annotated entities
├── dto/
│   ├── request/    – SocialAuthRequest, CreateOrderRequest, …
│   └── response/   – UserResponse, OrderResponse, …
├── security/       – CognitoJwtValidator, JwtSecurityFilter, AuthenticatedUserContext
├── client/         – RazorpayClient
├── exception/      – GlobalExceptionMapper, ApiException hierarchy
├── lambda/         – LambdaHandler (documentation)
├── common/         – ApiResponse, PaginatedResponse
└── util/           – IdGenerator, TimeUtil, JsonUtil
```

---

## Environment Variables

Set these in the Lambda function configuration or via AWS Secrets Manager.

| Variable | Description | Default |
|---|---|---|
| `AWS_REGION` | AWS region | `ap-south-1` |
| `COGNITO_REGION` | Cognito region | `ap-south-1` |
| `COGNITO_USER_POOL_ID` | Cognito User Pool ID | **Required** |
| `RAZORPAY_KEY_ID` | Razorpay API Key ID | **Required** |
| `RAZORPAY_KEY_SECRET` | Razorpay API Key Secret | **Required** |
| `RAZORPAY_WEBHOOK_SECRET` | Razorpay Webhook Secret | **Required** |
| `SES_SENDER_EMAIL` | SES sender address | `noreply@eventmacha.com` |
| `SES_REGION` | SES region | `ap-south-1` |
| `EVENT_MACHA_USERS_TABLE_NAME` | DynamoDB Users table | `Users` |
| `EVENT_MACHA_RATE_CARDS_TABLE_NAME` | DynamoDB RateCards table | `RateCards` |
| `EVENT_MACHA_RATE_CARD_PLANS_TABLE_NAME` | DynamoDB RateCardPlans table | `RateCardPlans` |
| `EVENT_MACHA_ORDERS_TABLE_NAME` | DynamoDB Orders table | `Orders` |
| `EVENT_MACHA_PAYMENTS_TABLE_NAME` | DynamoDB Payments table | `Payments` |
| `EVENT_MACHA_PAYMENT_HISTORY_TABLE_NAME` | DynamoDB PaymentHistory table | `PaymentHistory` |
| `EVENT_MACHA_PUBLISH_TABLE_NAME` | DynamoDB Publish table | `Publish` |
| `EVENT_MACHA_PUBLISH_HISTORY_TABLE_NAME` | DynamoDB PublishHistory table | `PublishHistory` |

---

## API Endpoints

### Authentication (open – no JWT required for social)
| Method | Path | Description |
|---|---|---|
| `POST` | `/auth/social` | Social / Email login via Cognito JWT |
| `GET` | `/auth/me` | Get current user profile |

### Orders (JWT required)
| Method | Path | Description |
|---|---|---|
| `POST` | `/orders` | Create a new order |
| `GET` | `/orders/{id}` | Get order by ID |

### Payments (JWT required)
| Method | Path | Description |
|---|---|---|
| `POST` | `/payments` | Initiate Razorpay payment |

### Webhooks (open – Razorpay signature verification)
| Method | Path | Description |
|---|---|---|
| `POST` | `/webhooks/razorpay` | Razorpay payment event webhook |

### Plans (public)
| Method | Path | Description |
|---|---|---|
| `GET` | `/plans` | Get active pricing plans |

### Publish (JWT required)
| Method | Path | Description |
|---|---|---|
| `POST` | `/publish/save-draft` | Save draft content |
| `POST` | `/publish/preview` | Generate preview token |
| `POST` | `/publish/live` | Publish live |
| `GET` | `/publish/{orderId}` | Get publish state |
| `GET` | `/publish/history/{orderId}` | Get publish history |

---

## Local Development

### Prerequisites
- Java 21+
- Maven 3.9+
- Docker (for DynamoDB Local)

### Run locally with DynamoDB Local

```bash
# Start DynamoDB Local
docker run -p 8000:8000 amazon/dynamodb-local

# Run Quarkus in dev mode
mvn quarkus:dev
```

Uncomment the local dev settings in `application.properties`:
```properties
%dev.quarkus.dynamodb.endpoint-override=http://localhost:8000
%dev.quarkus.dynamodb.aws.credentials.type=static
%dev.quarkus.dynamodb.aws.credentials.static-provider.access-key-id=local
%dev.quarkus.dynamodb.aws.credentials.static-provider.secret-access-key=local
```

### Run tests

```bash
mvn test
```

---

## Build for Lambda Deployment

```bash
# Build the Lambda ZIP
mvn clean package -DskipTests

# The ZIP is at:
ls -lh target/function.zip
```

---

## Lambda Deployment Commands

### Create / Update Lambda Function (AWS CLI)

```bash
# Package
mvn clean package -DskipTests

# Create function (first time)
aws lambda create-function \
  --function-name em-domain-apis \
  --runtime java21 \
  --role arn:aws:iam::YOUR_ACCOUNT_ID:role/em-lambda-role \
  --handler "io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest" \
  --zip-file fileb://target/function.zip \
  --memory-size 512 \
  --timeout 30 \
  --environment Variables="{
    COGNITO_USER_POOL_ID=ap-south-1_XXXXXXXXX,
    RAZORPAY_KEY_ID=rzp_live_XXXX,
    RAZORPAY_KEY_SECRET=XXXX,
    RAZORPAY_WEBHOOK_SECRET=XXXX
  }" \
  --region ap-south-1

# Update function code (subsequent deploys)
aws lambda update-function-code \
  --function-name em-domain-apis \
  --zip-file fileb://target/function.zip \
  --region ap-south-1
```

### Add API Gateway trigger

```bash
aws lambda add-permission \
  --function-name em-domain-apis \
  --statement-id apigateway-invoke \
  --action lambda:InvokeFunction \
  --principal apigateway.amazonaws.com \
  --source-arn "arn:aws:execute-api:ap-south-1:YOUR_ACCOUNT_ID:YOUR_API_ID/*"
```

---

## Lambda Handler

```
io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest
```

Set this as the handler in your Lambda function configuration.

---

## Razorpay Webhook Configuration

1. In Razorpay Dashboard → Settings → Webhooks
2. Set URL: `https://api.eventmacha.com/webhooks/razorpay`
3. Select events: `payment.captured`, `payment.failed`, `payment.authorized`
4. Copy the **Webhook Secret** and set it as `RAZORPAY_WEBHOOK_SECRET` in Lambda env vars

---

## OpenAPI Documentation

When running locally:
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/openapi`

---

## IAM Permissions Required for Lambda Role

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:GetItem",
        "dynamodb:PutItem",
        "dynamodb:UpdateItem",
        "dynamodb:DeleteItem",
        "dynamodb:Query",
        "dynamodb:Scan"
      ],
      "Resource": [
        "arn:aws:dynamodb:ap-south-1:*:table/Users",
        "arn:aws:dynamodb:ap-south-1:*:table/Users/index/*",
        "arn:aws:dynamodb:ap-south-1:*:table/RateCards",
        "arn:aws:dynamodb:ap-south-1:*:table/RateCards/index/*",
        "arn:aws:dynamodb:ap-south-1:*:table/RateCardPlans",
        "arn:aws:dynamodb:ap-south-1:*:table/Orders",
        "arn:aws:dynamodb:ap-south-1:*:table/Orders/index/*",
        "arn:aws:dynamodb:ap-south-1:*:table/Payments",
        "arn:aws:dynamodb:ap-south-1:*:table/Payments/index/*",
        "arn:aws:dynamodb:ap-south-1:*:table/PaymentHistory",
        "arn:aws:dynamodb:ap-south-1:*:table/Publish",
        "arn:aws:dynamodb:ap-south-1:*:table/PublishHistory"
      ]
    },
    {
      "Effect": "Allow",
      "Action": ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"],
      "Resource": "arn:aws:logs:*:*:*"
    }
  ]
}
```