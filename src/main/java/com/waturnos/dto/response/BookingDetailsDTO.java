package com.waturnos.dto.response;

import java.util.List;

import com.waturnos.dto.beans.BookingDTO;
import com.waturnos.dto.beans.ClientDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookingDetailsDTO extends BookingDTO{
	
	private List<ClientDTO> clients;

}
