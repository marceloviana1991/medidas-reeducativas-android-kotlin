package marceloviana1991.medidasreeducativas

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MedidaReeducativaDao {

    @Query("SELECT * FROM MedidaReeducativa")
    suspend fun buscaTodas(): List<MedidaReeducativa>

    @Insert
    suspend fun salva(medidaReeducativa: MedidaReeducativa)

    @Delete
    suspend fun exclui(medidaReeducativa: MedidaReeducativa)

    @Update
    suspend fun edita(medidaReeducativa: MedidaReeducativa)

}