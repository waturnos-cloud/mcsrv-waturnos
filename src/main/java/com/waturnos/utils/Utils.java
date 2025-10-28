package com.waturnos.utils;

public class Utils {
	
	
	/**
	 * Builds the password.
	 *
	 * @param name the name
	 * @param phone the phone
	 * @return the string
	 */
	public static String buildPassword(String name, String phone) {
        // Asegurar que name no sea nulo y eliminar espacios extra
        String trimmedName = (name != null) ? name.trim() : "";
        String parteNombre;

        // 1. Determinar la parte del nombre
        if (trimmedName.isEmpty()) {
            parteNombre = "";
        } else {
            // Dividir por espacios para identificar palabras
            String[] partes = trimmedName.split("\\s+");
            
            if (partes.length > 1) {
                // Caso 1: Nombre con apellido(s) (más de una palabra)
                String ultimoApellido = partes[partes.length - 1];
                int len = ultimoApellido.length();
                // Tomar los últimos 5 del apellido
                parteNombre = ultimoApellido.substring(Math.max(0, len - 5));
            } else {
                // Caso 2: Nombre de una sola palabra
                // Tomar los primeros 5 del nombre
                parteNombre = trimmedName.substring(0, Math.min(trimmedName.length(), 5));
            }
            // Convertir a minúsculas
            parteNombre = parteNombre.toLowerCase();
        }

        // 2. Determinar la parte del teléfono (últimos 3 dígitos)
        // Usar Math.max para asegurar que no se produzca un error si el teléfono es muy corto
        String parteTelefono = (phone != null && phone.length() >= 3)
            ? phone.substring(phone.length() - 3)
            : "";

        return parteNombre + parteTelefono;
    }

}
