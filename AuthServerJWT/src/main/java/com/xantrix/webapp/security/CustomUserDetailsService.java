package com.xantrix.webapp.security;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service("customUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService
{
	private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
	
	@Autowired
	private UserConfig Config;
	
	@Override
	public UserDetails loadUserByUsername(String UserId) 
			throws UsernameNotFoundException
	{
		String ErrMsg = "";
		
		if (UserId == null || UserId.length() < 2) 
		{
			ErrMsg = "Nome utente assente o non valido";
			
			logger.warn(ErrMsg);
			
	    	throw new UsernameNotFoundException(ErrMsg); 
		} 
		
		Utenti utente = this.GetHttpValue(UserId);
		
		if (utente == null)
		{
			ErrMsg = String.format("Utente %s non Trovato!!", UserId);
			
			logger.warn(ErrMsg);
			
			throw new UsernameNotFoundException(ErrMsg);
		}
		
		UserBuilder builder = null;
		builder = org.springframework.security.core.userdetails.User.withUsername(utente.getUserId());
		builder.disabled((utente.getAttivo().equals("Si") ? false : true));
		builder.password(utente.getPassword());
		
		String[] profili = utente.getRuoli()
				 .stream().map(a -> "ROLE_" + a).toArray(String[]::new);
		
		builder.authorities(profili);
		
		return builder.build();
		
		
	}
	
	private Utenti GetHttpValue(String UserId)
	{
		URI url = null;

		try 
		{
			String SrvUrl = Config.getSrvUrl();

			url = new URI(SrvUrl + UserId);
		} 
		catch (URISyntaxException e) 
		{
			 
			e.printStackTrace();
		}
		
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(Config.getUserId(), Config.getPassword()));
		
		Utenti utente = null;

		try 
		{
			utente = restTemplate.getForObject(url, Utenti.class);	
		} 
		catch (Exception e) 
		{
			String ErrMsg = String.format("Connessione al servizio di autenticazione non riuscita!!");
			
			logger.warn(ErrMsg);
			
		}
		
		return utente;
		
	}
	
	
	
	
	
	
	
	
}
	