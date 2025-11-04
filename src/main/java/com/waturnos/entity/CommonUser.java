package com.waturnos.entity;

public interface CommonUser {
	
    Long getId();
    String getEmail();
    String getPassword();
    String getFullName();
    String getPhone();
    
    String getUserType();

}
