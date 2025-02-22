package marceloviana1991.medidasreeducativas

import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.Period

@Entity
@Parcelize
class MedidaReeducativa(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var interno: String,
    var intervencao: String,
    var prazo: String,
    val localDate: LocalDate
) : Parcelable {


    override fun toString(): String {
        return "$interno - $localDate\n" +
                "$intervencao - $prazo dias"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verificaPrazo(): Boolean {
        val dias = Period.between(localDate, LocalDate.now()).days
        return dias >= prazo.toInt()
    }
}