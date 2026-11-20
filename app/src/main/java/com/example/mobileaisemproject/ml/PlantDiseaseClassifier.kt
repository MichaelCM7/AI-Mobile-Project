package com.example.mobileaisemproject.ml

data class DiseaseRecord(
    val id: String,
    val name: String,
    val confidence: String,
    val date: String
)

class PlantDiseaseClassifier {
    // TODO: Load crop_disease_model.onnx, labels.txt, and remedy-map.json from assets
}