// In: com.example.nabthespy.ml.FaceMatcher.kt

package com.example.nabthespy.ml

import kotlin.math.pow
import kotlin.math.sqrt

object FaceMatcher {

    // A threshold for deciding if two faces are the same person.
    // This value is crucial and needs to be tuned. A common starting point for
    // L2 normalized embeddings is around 1.0 to 1.2.
    private const val SIMILARITY_THRESHOLD = 1.05f

    /**
     * Compares two face embeddings to see if they belong to the same person.
     * @param savedEmbedding The embedding of the owner stored in the app.
     * @param currentEmbedding The embedding of the person currently looking at the screen.
     * @return True if the faces are considered a match, false otherwise.
     */
    fun areFacesSame(savedEmbedding: FloatArray, currentEmbedding: FloatArray): Boolean {
        if (savedEmbedding.size != currentEmbedding.size) {
            return false
        }
        val distance = calculateEuclideanDistance(savedEmbedding, currentEmbedding)
        return distance < SIMILARITY_THRESHOLD
    }

    /**
     * Calculates the Euclidean (L2) distance between two vectors.
     */
    private fun calculateEuclideanDistance(embedding1: FloatArray, embedding2: FloatArray): Float {
        var sum = 0.0f
        for (i in embedding1.indices) {
            val diff = embedding1[i] - embedding2[i]
            sum += diff.pow(2)
        }
        return sqrt(sum)
    }
}