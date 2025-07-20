Write-Output "Criando tópico SNS..."
$topicArn = aws --endpoint-url=http://localhost:4566 sns create-topic --name pedido-criado-topic | ConvertFrom-Json | Select-Object -ExpandProperty TopicArn

Write-Output "Criando fila SQS..."
$queueUrl = aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name PedidosFila | ConvertFrom-Json | Select-Object -ExpandProperty QueueUrl

Write-Output "Obtendo ARN da fila SQS..."
$queueArn = aws --endpoint-url=http://localhost:4566 sqs get-queue-attributes --queue-url $queueUrl --attribute-name QueueArn | ConvertFrom-Json | ForEach-Object { $_.Attributes.QueueArn }

Write-Output "Configurando permissões da fila SQS para o tópico SNS..."

$policy = @"
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": "*",
      "Action": "SQS:SendMessage",
      "Resource": "$queueArn",
      "Condition": {
        "ArnEquals": {
          "aws:SourceArn": "$topicArn"
        }
      }
    }
  ]
}
"@

# Salva a policy temporariamente em um arquivo para evitar erros de parsing
$tempFile = New-TemporaryFile
$policy | Out-File -FilePath $tempFile -Encoding utf8

aws --endpoint-url=http://localhost:4566 sqs set-queue-attributes `
    --queue-url $queueUrl `
    --attributes Policy="file://$tempFile"

# Remove o arquivo temporário
Remove-Item $tempFile

Write-Output "Inscrevendo a fila no tópico SNS..."
aws --endpoint-url=http://localhost:4566 sns subscribe `
    --topic-arn $topicArn `
    --protocol sqs `
    --notification-endpoint $queueArn

# Write-Output "Criando tabela DynamoDB..."
# aws --endpoint-url=http://localhost:4566 dynamodb create-table --cli-input-json file://dynamodb-pedidos.json
