package cn.bugstack.sdk.infrastructure.openai;

import cn.bugstack.sdk.infrastructure.openai.dto.ChatCompletionRequestDTO;
import cn.bugstack.sdk.infrastructure.openai.dto.ChatCompletionSyncResponseDTO;

public interface IOpenAi {
    ChatCompletionSyncResponseDTO completions(ChatCompletionRequestDTO requestDTO) throws Exception;

}
