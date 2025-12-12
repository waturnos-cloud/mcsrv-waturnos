package com.waturnos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.User;

/**
 * Repositorio para gestionar propiedades de usuarios en la tabla users_props.
 */
public interface UserPropsRepository extends JpaRepository<User, Long> {
	
	/**
	 * Obtiene el valor de una propiedad de usuario.
	 *
	 * @param userId el ID del usuario
	 * @param key la clave de la propiedad
	 * @return el valor de la propiedad
	 */
	@Query(value = "SELECT value FROM users_props WHERE users_id = :userId AND key = :key", nativeQuery = true)
	Optional<String> findUserPropValue(@Param("userId") Long userId, @Param("key") String key);
	
	/**
	 * Verifica si existe una propiedad para un usuario.
	 *
	 * @param userId el ID del usuario
	 * @param key la clave de la propiedad
	 * @return true si existe, false si no
	 */
	@Query(value = "SELECT COUNT(*) > 0 FROM users_props WHERE users_id = :userId AND key = :key", nativeQuery = true)
	boolean existsUserProp(@Param("userId") Long userId, @Param("key") String key);
	
	/**
	 * Inserta una nueva propiedad de usuario.
	 *
	 * @param userId el ID del usuario
	 * @param key la clave de la propiedad
	 * @param value el valor de la propiedad
	 */
	@Modifying
	@Query(value = "INSERT INTO users_props (users_id, key, value) VALUES (:userId, :key, :value)", nativeQuery = true)
	void insertUserProp(@Param("userId") Long userId, @Param("key") String key, @Param("value") String value);
	
	/**
	 * Actualiza una propiedad existente de usuario.
	 *
	 * @param userId el ID del usuario
	 * @param key la clave de la propiedad
	 * @param value el nuevo valor de la propiedad
	 */
	@Modifying
	@Query(value = "UPDATE users_props SET value = :value WHERE users_id = :userId AND key = :key", nativeQuery = true)
	void updateUserProp(@Param("userId") Long userId, @Param("key") String key, @Param("value") String value);
	
	/**
	 * Elimina una propiedad de usuario.
	 *
	 * @param userId el ID del usuario
	 * @param key la clave de la propiedad
	 * @return el número de filas eliminadas
	 */
	@Modifying
	@Query(value = "DELETE FROM users_props WHERE users_id = :userId AND key = :key", nativeQuery = true)
	int deleteUserProp(@Param("userId") Long userId, @Param("key") String key);
}
