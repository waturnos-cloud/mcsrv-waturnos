package com.waturnos.service;

import java.util.List;

import com.waturnos.dto.request.AddPaymentRequest;
import com.waturnos.dto.response.PaymentProviderResponse;
import com.waturnos.enums.PaymentProviderType;

/**
 * Servicio para gestionar proveedores de pago vinculados a usuarios.
 */
public interface PaymentProviderService {
	
	/**
	 * Vincula o actualiza un proveedor de pago para un usuario.
	 *
	 * @param userId el ID del usuario
	 * @param request los datos del proveedor de pago
	 */
	void addPaymentProvider(Long userId, AddPaymentRequest request);
	
	/**
	 * Obtiene la configuración de un proveedor de pago para un usuario.
	 *
	 * @param userId el ID del usuario
	 * @param type el tipo de proveedor
	 * @return la configuración del proveedor o null si no existe
	 */
	PaymentProviderResponse getPaymentProvider(Long userId, PaymentProviderType type);
	
	/**
	 * Obtiene todos los proveedores de pago configurados para un usuario.
	 *
	 * @param userId el ID del usuario
	 * @return lista de proveedores configurados (puede estar vacía)
	 */
	List<PaymentProviderResponse> getAllPaymentProviders(Long userId);
	
	/**
	 * Elimina un proveedor de pago vinculado a un usuario.
	 *
	 * @param userId el ID del usuario
	 * @param type el tipo de proveedor
	 */
	void removePaymentProvider(Long userId, PaymentProviderType type);
}
