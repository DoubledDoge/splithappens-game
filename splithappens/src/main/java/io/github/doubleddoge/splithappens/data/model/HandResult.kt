package io.github.doubleddoge.splithappens.data.model

enum class HandResult {
	IN_PROGRESS, // Might never reach database, rounds written at the end
	WIN,
	LOSS,
	PUSH
}