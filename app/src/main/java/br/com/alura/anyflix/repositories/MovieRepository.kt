package br.com.alura.anyflix.repositories

import br.com.alura.anyflix.database.dao.MovieDao
import br.com.alura.anyflix.database.entities.toMovie
import br.com.alura.anyflix.model.Movie
import br.com.alura.anyflix.network.services.MovieService
import br.com.alura.anyflix.network.services.toMovieEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class MovieRepository @Inject constructor(
    private val dao: MovieDao,
    private val service: MovieService
) {

    suspend fun findSections(): Flow<Map<String, List<Movie>>> {

        CoroutineScope(coroutineContext).launch(Dispatchers.IO){
            val response = service.findAll()
            val entities = response.map {
                it.toMovieEntity()
            }
            dao.saveAll(*entities.toTypedArray())
        }

        return dao.findAll()
            .map { entitys ->
                val movies = entitys.map { it.toMovie() }
                if (movies.isEmpty()) {
                    emptyMap()
                } else {
                    createSections(movies)
                }
            }

    }

    private fun createSections(movies: List<Movie>) = mapOf(
        "Em alta" to movies.shuffled().take(7),
        "Novidades" to movies.shuffled().take(7),
        "Continue assistindo" to movies.shuffled().take(7)
    )

}