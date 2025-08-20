package com.samikhan.draven.data.model

import com.samikhan.draven.ui.components.AIModel

data class ModelConfig(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: String,
    val apiEndpoint: String,
    val apiKey: String,
    val modelName: String,
    val maxTokens: Int,
    val temperature: Float,
    val isNew: Boolean = false,
    val isMax: Boolean = false
)

class AIModelManager {
    
    companion object {
        private var currentModelId: String = "nvidia-nemotron"
        private val modelConfigs = mutableMapOf<String, ModelConfig>()
        
        init {
            // Initialize with default models
            setupDefaultModels()
        }
        
        private fun setupDefaultModels() {
            // NVIDIA NeMoTron (Default)
            modelConfigs["nvidia-nemotron"] = ModelConfig(
                id = "nvidia-nemotron",
                name = "NeMoTron Ultra",
                subtitle = "NVIDIA's powerful reasoning model",
                category = "Best",
                apiEndpoint = "https://integrate.api.nvidia.com/v1/chat/completions",
                apiKey = "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",
                modelName = "nvidia/llama-3.1-nemotron-ultra-253b-v1",
                maxTokens = 1024,
                temperature = 0.1f
            )
            
            // GPT-OSS for Critical Thinking
            modelConfigs["gpt-oss"] = ModelConfig(
                id = "gpt-oss",
                name = "GPT-OSS",
                subtitle = "Open source GPT for critical thinking",
                category = "Reasoning",
                apiEndpoint = "https://integrate.api.nvidia.com/v1/chat/completions",
                apiKey = "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",
                modelName = "openai/gpt-oss-120b",
                maxTokens = 4096,
                temperature = 1.0f,
                isMax = true
            )
            

        }
        
        fun addModel(config: ModelConfig) {
            modelConfigs[config.id] = config
        }
        
        fun getCurrentModel(): ModelConfig? {
            return modelConfigs[currentModelId]
        }
        
        fun setCurrentModel(modelId: String) {
            if (modelConfigs.containsKey(modelId)) {
                currentModelId = modelId
            }
        }
        
        fun getAllModels(): List<AIModel> {
            return modelConfigs.values.map { config ->
                AIModel(
                    id = config.id,
                    name = config.name,
                    subtitle = when {
                        config.id == "nvidia-nemotron" -> "NVIDIA's powerful model (may have queues)"
                        config.id == "gpt-oss" -> "Open source GPT for critical thinking (faster)"
                        else -> config.subtitle
                    },
                    category = config.category,
                    isSelected = config.id == currentModelId,
                    isNew = config.isNew,
                    isMax = config.isMax,
                    isAvailable = true,
                    isHighTraffic = config.id == "nvidia-nemotron", // NeMoTron has high traffic
                    recommendedForSpeed = config.id == "gpt-oss" // GPT-OSS is faster
                )
            }
        }
        
        fun getModelConfig(modelId: String): ModelConfig? {
            return modelConfigs[modelId]
        }
        
        fun getCurrentModelId(): String {
            return currentModelId
        }
        
        fun updateModelConfig(modelId: String, updatedConfig: ModelConfig) {
            modelConfigs[modelId] = updatedConfig
        }
    }
}
