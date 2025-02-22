package marceloviana1991.medidasreeducativas

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MedidaReeducativaDao {

    @Query("SELECT * FROM MedidaReeducativa")
    fun buscaTodas(): List<MedidaReeducativa>

    @Insert
    fun salva(medidaReeducativa: MedidaReeducativa)

    @Delete
    fun exclui(medidaReeducativa: MedidaReeducativa)

    @Update
    fun edita(medidaReeducativa: MedidaReeducativa)

}