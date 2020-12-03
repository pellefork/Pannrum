package se.fork.pannrum.model

data class TempRecord(
    val boilerTemp: Float? = null,
    val smokeTemp: Float? = null,
    val pumpLeftTemp: Float? = null,
    val pumpRightTemp: Float? = null,
    val pumpTopTemp: Float? = null,
    val accTank1TopTemp: Float? = null,
    val accTank1BottomTemp: Float? = null,
    val accTank2TopTemp: Float? = null,
    val accTank2BottomTemp: Float? = null,
    val accTank3TopTemp: Float? = null,
    val accTank3BottomTemp: Float? = null
) {
    companion object {
        fun createTestRecord() : TempRecord {
            val rec = TempRecord(
                boilerTemp = 73.2f,
                smokeTemp = 180f,
                pumpLeftTemp = 32.2f,
                pumpRightTemp = 40f,
                pumpTopTemp = 70f,
                accTank1TopTemp = 74.2f,
                accTank1BottomTemp = 26f,
                accTank2TopTemp = 75f,
                accTank2BottomTemp = 27f,
                accTank3TopTemp = 74f,
                accTank3BottomTemp = 28f
            )
            return rec
        }
    }
}