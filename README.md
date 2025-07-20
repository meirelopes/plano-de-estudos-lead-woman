# Projeto: API de Pedidos com LocalStack e AWS SQS

Este projeto foi desenvolvido como parte do plano de estudos da mentoria Lead Woman. O objetivo é criar uma API REST em Java com Spring Boot para enviar pedidos para uma fila SQS simulada com o LocalStack.
## 🗂️ Índice

- [🧠 Objetivo do Projeto](#-objetivo-do-projeto)
- [🛠️ Decisões Tomadas](#-decisões-tomadas)
- [📦 Estrutura do Projeto](#-estrutura-do-projeto)
- [🚀 Etapas Executadas](#-etapas-executadas)
- [🧪 Testes Realizados](#-testes-realizados)
- [📚 Links Úteis](#-links-úteis)
- [✍️ Anotações Pessoais](#️-anotações-pessoais)

---

## 🧠 Objetivo do Projeto

* Simular serviços da AWS localmente (SQS, SNS, Lambda, DynamoDB, API Gateway).
* Criar uma API REST que envia pedidos para uma fila SQS.
* Usar Docker + LocalStack para testes locais.

---

## 🛠️ Decisões Tomadas

* Linguagem escolhida: **Java com Spring Boot**.
* Gerenciador de dependências: **Maven**.
* Serviços simulados com LocalStack: `sqs`, `sns`, `lambda`, `dynamodb`, `apigateway`.
* Uso de `docker-compose` para configurar LocalStack.
* Criação dos recursos simulados (SNS, SQS, etc.) feita via **script PowerShell (`.ps1`)**, que automatiza os comandos da **AWS CLI apontando para o LocalStack** via `--endpoint-url=http://localhost:4566`.



---

## 📦 Estrutura do Projeto

```
leadwoman/
├── src/
├── pom.xml
├── docker-compose.yml
├── setup-localstack.ps1
├── application.properties
└── README-localstack-sqs-api.md (este arquivo)
```

---

## 🚀 Etapas Executadas
### 1. Desenvolvimento da API REST

* Endpoint: `POST /pedidos`
* Payload JSON:

```json
{
  "pedidoId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "cliente": "João",
  "produto": "Camiseta",
  "quantidade": 2
}
```

### 2. Amazon SNS
O **Amazon SNS (Simple Notification Service)** é um serviço de mensagens que permite enviar notificações para diferentes destinos (como filas SQS, funções Lambda, ou e-mails). Em Java, usamos a AWS SDK para interagir com o SNS.

#### 🔄 Fluxo básico de funcionamento:
✅ Você cria um tópico SNS.

✅ Você cria um assinante (por exemplo, uma fila SQS).

✅ Você inscreve essa fila no tópico.

✅ Quando alguém publica uma mensagem no tópico SNS...

📩 ...todos os assinantes recebem essa mensagem.
#### Dependência Utilizada
```xml
    <dependency>
      <groupId>com.amazonaws</groupId>
      <artifactId>aws-java-sdk-sns</artifactId>
      <version>1.12.787</version>
    </dependency>
```

#### Classes importantes nesse processo:

#### 🔹 `PublishRequest`

A classe `PublishRequest` é usada para **criar a requisição de envio da mensagem**. Ela representa os dados que serão enviados ao tópico SNS.

**Principais parâmetros:**
- `topicArn`: o ARN do tópico SNS.
- `message`: o conteúdo da mensagem (geralmente um JSON).
- `subject` (opcional): um título para a mensagem (usado em envio por e-mail).
- `messageAttributes` (opcional): atributos personalizados da mensagem (usados, por exemplo, para filtros de assinatura).

#### 🔹 `PublishResult`

A classe `PublishResult` representa o resultado do envio da mensagem ao SNS. É o retorno da chamada `snsClient.publish(...)`.

**Principais informações:**
- `messageId`: um ID único da mensagem publicada (útil para rastreamento e logs).

#### 🔹 `AmazonSNS` (Interface)

A interface `AmazonSNS` representa o **cliente para interagir com o serviço SNS**.

**O que ela faz?**
- Fornece os métodos para publicar mensagens, criar tópicos, gerenciar assinaturas e mais.
- A implementação comum é a classe `AmazonSNSClient`.

### 3. Amazon SQS (Simple Queue Service)

O Amazon Simple Queue Service (SQS) é um serviço de fila de mensagens totalmente gerenciado que permite desacoplar e escalar microsserviços, sistemas distribuídos e aplicações sem servidor.

**Para que serve o SQS?**

- **Comunicação Assíncrona:** Permite que componentes de um sistema troquem mensagens sem precisar responder imediatamente, aumentando a resiliência.
- **Desacoplamento:** Serviços produtores e consumidores ficam independentes, o que facilita a manutenção e escalabilidade.
- **Gerenciamento de picos:** Ajuda a lidar com variações na carga de trabalho ao armazenar temporariamente mensagens até que o consumidor esteja pronto para processá-las.

**Como funciona?**

1. **Produtor envia mensagens para a fila SQS.**
2. **Fila armazena as mensagens de forma segura e durável.**
3. **Consumidor busca (recebe) as mensagens na fila para processamento.**
4. Após o processamento, o consumidor pode deletar a mensagem da fila para evitar reprocessamento.

**Características principais:**

- **Entrega “pelo menos uma vez”**: Mensagens podem ser entregues mais de uma vez, então a aplicação deve ser idempotente.
- **Ordem não garantida (padrão)**: Mensagens podem chegar fora de ordem, embora exista a opção de fila FIFO para garantir ordem e duplicidade.
- **Escalabilidade automática:** SQS gerencia automaticamente o throughput da fila.
- **Integração com outros serviços AWS:** Como Lambda, SNS, EC2, entre outros.
- **Altamente disponível e durável:** As mensagens são replicadas em múltiplas zonas de disponibilidade.

No projeto o SQS está sendo usado para receber pedidos enviados pela API. Quando um pedido é criado, uma mensagem é colocada na fila `PedidosFila`. Outros componentes ou serviços podem então consumir essas mensagens para processar os pedidos de forma desacoplada e escalável.

### 4. Configuração do LocalStack com Docker

* Docker e AWS CLI instalados.
* `docker-compose.yml` criado na raiz do projeto.
```yaml
version: '3.8'
services:
  localstack:
    image: localstack/localstack:latest
    container_name: localstack
    ports:
      - "4566:4566"        # Porta principal (Edge service)
      - "4571:4571"        # Porta para debug (opcional)
    environment:
      - SERVICES=sqs,sns,lambda,dynamodb,apigateway
      - DEBUG=1
      - DATA_DIR=/tmp/localstack/data
      - LAMBDA_EXECUTOR=docker-reuse
      - DOCKER_HOST=unix:///var/run/docker.sock
    volumes:
      - "/var/run/docker.sock:/var/run/docker.sock"
```
* Script PowerShell `setup-localstack.ps1` na raíz do projeto, para Configuração LocalStack.
```powershell
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
```
---

## 🧪 Testes Realizados
Para verificar se tudo funcionou corretamente:

1. Verificando se o tópico SNS foi criado:
```bash
aws --endpoint-url=http://localhost:4566 sns list-topics
```

2. Verificando se há uma inscrição (subscription) do SNS na SQS:
```bash
aws --endpoint-url=http://localhost:4566 sns list-subscriptions
```

3. Verificando se a fila foi criada:
```bash
aws --endpoint-url=http://localhost:4566 sqs list-queues
```

4. Verificando se há mensagens na fila (receber mensagens):
```bash
aws --endpoint-url=http://localhost:4566 sqs receive-message --queue-url http://localhost:4566/000000000000/PedidosFila
```

* Envio de pedido via Postman.
```bash
curl --request POST \
  --url http://localhost:8080/pedidos \
  --header 'Content-Type: application/json' \
  --header 'User-Agent: insomnia/2023.5.8' \
  --data '{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "cliente": "João Silva",
  "produto": "Camiseta",
  "quantidade": 3
}
'
```
---

## 📚 Links Úteis

* [LocalStack Getting Started](https://docs.localstack.cloud/getting-started/)
* [AWS CLI with LocalStack](https://docs.localstack.cloud/aws/integrations/aws-native-tools/aws-cli/)
* [Criar fila SQS com AWS CLI](https://docs.aws.amazon.com/cli/latest/reference/sqs/create-queue.html)
* [AWS SDK for Java - SQS](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-sqs.html)


---

## ✍️ Anotações Pessoais

* [ ] Adicionar testes automatizados com JUnit.
* [ ] Explorar integração com Lambda.
* [ ] Persistir pedido na tabela DynamoDB (futuramente).

---

**Autora:** Meire Lopes
**Mentora:** 
**Data de início:** 19/07/2025
