package com.mock.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;

import org.minnal.autopojo.AutoPojoFactory;

@Path("/zip/services")
public class ZipCodeREST {

	@XmlElement(name = "zipcode")
	static Map<String, ZipCodeBean> data;

	static ZipCodeBean createItem(String zipCode, String streetName, String neighborhood, String city, String state,
			String country) {

		ZipCodeBean zc = new ZipCodeBean();
		zc.setZipCode(zipCode);
		zc.setCity(city);
		zc.setCountry(country);
		zc.setNeighborhood(neighborhood);
		zc.setState(state);
		zc.setStreetName(streetName);

		return zc;
	}

	static {
		data = new HashMap<String, ZipCodeBean>();
		data.put("99501", createItem("99501", "Lawrence St.", "Center", "Some City", "UK", "USA"));
		data.put("99700000", createItem("99700000", "Test St.", "Center", "Some City", "RS", "Brazil"));
		data.put("65000", createItem("65000", null, "Center", null, null, "Ukraine"));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getItems(@QueryParam("neighborhood") String neighborhood, @QueryParam("city") String city) {
		AutoPojoFactory factory = new AutoPojoFactory();
		ZipCodeBean result = factory.populate(ZipCodeBean.class, 3);
		return Response.ok(new ZipCodeBean[] { result }).build();
	}

	@GET
	@Path("{zipCode}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getItem(@PathParam("zipCode") String zipCode) {
		return Response.ok(data.get(zipCode)).build();
	}
}
