package com.waturnos.dto.response;

import java.util.List;

import com.waturnos.dto.beans.BookingDTO;
import com.waturnos.dto.beans.ClientDTO;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class BookingDetailsDTO extends BookingDTO{
	
	private List<ClientDTO> clients;

}
