package com.dreamypatisiel.devdevdev.openai.embeddings;

import java.util.List;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

public class CosineSimilarityCalculator {

    /**
     * @Note: embeddingA 과 embeddingB의 코사인 유사도 계산
     * @Author: 장세웅
     * @Since: 2024.06.09
     */
    public static double cosineSimilarity(List<Double> embeddingA, List<Double> embeddingB) {

        RealVector vectorA = new ArrayRealVector(embeddingA.stream()
                .mapToDouble(Double::doubleValue)
                .toArray());

        RealVector vectorB = new ArrayRealVector(embeddingB.stream()
                .mapToDouble(Double::doubleValue)
                .toArray());

        // vectorA vectorB 내적
        double dotProduct = vectorA.dotProduct(vectorB);
        double normA = vectorA.getNorm();
        double normB = vectorB.getNorm();

        return dotProduct / (normA * normB);
    }
}
