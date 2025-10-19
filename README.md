# playground-ai-chat-cli

A chat cli using LLM with tools

## Starting

Proxy & Otel

```shell
docker compose up -d
```

## Ollama

Download and install olama

### Installing Embedding

```shell
ollama pull mxbai-embed-large
```

### Installing Reasoning Model

Supports reasoning, but it’s best viewed as a balanced 
instruction-following model — solid for tool orchestration, 
structured generation, and short reasoning chains

```shell
ollama pull llama3.1:8b-instruct-q4_K_M
```

For semantic disambiguation + CoT (chain-of-thought) reliability

```shell
ollama pull deepseek-r1:8b
```

#### Testing with curl

```shell
curl -X POST http://localhost:30001/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama3.1:8b-instruct-q4_K_M",
    "prompt": "Say hello in one sentence."
  }'
```


```shell
 curl -v POST http://localhost:30001/api/generate \
    -H "Content-Type: application/json" \
    -d '{"model":"mixtral:8x7b","prompt":"Say hi"}'
```