package com.ashwin.RestDemo.restapi;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.ashwin.RestDemo.config.ApplicationContextProvider;
import com.ashwin.RestDemo.model.Car;
import com.ashwin.RestDemo.model.Deal;
import com.ashwin.RestDemo.model.Link;
import com.ashwin.RestDemo.service.InventoryService;
import com.google.gson.Gson;

@Path("/cars")
public class InventoryHandler {
	private InventoryService inventoryService = null;

	@POST
	@RolesAllowed("SELLER")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
	public String addForSale(@HeaderParam("Authorization") String authCredentials, Car car, @Context UriInfo uriInfo) {

		Gson gson = new Gson();
		//Car car = gson.fromJson(json, Car.class);
		if (inventoryService == null) {
			inventoryService = (InventoryService) ApplicationContextProvider.getApplicationContext()
					.getBean("inventoryService");
		}
		car = inventoryService.listForSale(car, authCredentials);

		if (car == null) {
			return "{\"Error\": \"Car could not be listed for Sale\"}";
		}

		List<Link> links = new LinkedList<Link>();
		links.add(getLinkForCarDetails(uriInfo, car.getId()));
		links.add(getLinkForCarList(uriInfo));
		car.setLinks(links);

		return gson.toJson(car);
	}



	@GET
	@RolesAllowed({ "SELLER", "BUYER" })
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	public String listForSale(@Context UriInfo uriInfo) {

		if (inventoryService == null) {
			inventoryService = (InventoryService) ApplicationContextProvider.getApplicationContext()
					.getBean("inventoryService");
		}
		List<Car> carList = inventoryService.listAllForSale();
		if (carList == null || carList.isEmpty()) {
			Link link = getLinkForAddingCar(uriInfo);
			return "{\"Error\": \"No cars listed for sale!\""
			+ ",\"uri\": \"" + link.getUri() + "\","
		     + "\"rel\": \"" + link.getRel() + "\""
		     + "}";
		}
		for(Car car : carList) {
			List<Link> links = new LinkedList<Link>();
			links.add(getLinkForCarDetails(uriInfo, car.getId()));
			links.add(getLinkForBuyingCar(uriInfo, car.getId()));
			car.setLinks(links);
		}

		return new Gson().toJson(carList);

	}

	@GET
	@Path("/buy/{carId}")
	@RolesAllowed("BUYER")
	@Produces(MediaType.APPLICATION_JSON)
	public String buyCar(@PathParam("carId") int carId, @HeaderParam("Authorization") String authCredentials, @Context UriInfo uriInfo) {

		if (inventoryService == null) {
			inventoryService = (InventoryService) ApplicationContextProvider.getApplicationContext()
					.getBean("inventoryService");
		}

		Deal deal = inventoryService.buy(carId, authCredentials);
		if(deal == null) {
			Link link = getLinkForCarList(uriInfo);
			return "{\"Error\": \"Could not buy the car\""
			     + ",\"uri\": \"" + link.getUri() + "\","
			     + "\"rel\": \"" + link.getRel() + "\""
			     + "}";
		}
		List<Link> links = new LinkedList<>();
		links.add(getLinkForCarDetails(uriInfo, deal.getCar().getId()));
		links.add(getLinkForCarList(uriInfo));
		deal.setLinks(links);

		return new Gson().toJson(deal);
	}

	@GET
	@RolesAllowed("SELLER")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/soldCars")
	public String listCarsSold(@HeaderParam("Authorization") String authCredentials, @Context UriInfo uriInfo) {
		if (inventoryService == null) {
			inventoryService = (InventoryService) ApplicationContextProvider.getApplicationContext()
					.getBean("inventoryService");
		}
		List<Car> carList = inventoryService.listCarsSold(authCredentials);

		if(carList == null || carList.isEmpty()) {
			Link listLink = getLinkForCarList(uriInfo);
			Link addLink = getLinkForAddingCar(uriInfo);
			String errorString = "{\"Error\": \"No cars sold by you!\""
				     + ",\"links\":[{\"uri\": \"" + addLink.getUri() + "\","
				     + "\"rel\": \"" + addLink.getRel() + "\"}"
				     + ",{\"uri\": \"" + listLink.getUri() + "\","
				     + "\"rel\": \"" + listLink.getRel() + "\"}]"
				     + "}";
			return errorString;
		}

		List<Link> links = null;
		for(Car car : carList) {
			links = new LinkedList<>();
			links.add(getLinkForCarDetails(uriInfo, car.getId()));
			links.add(getLinkForCarList(uriInfo));
			car.setLinks(links);
		}
		return new Gson().toJson(carList);
	}

	@GET
	@RolesAllowed({"BUYER","SELLER"})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{carId}")
	public String getCarDetails(@PathParam("carId") int carId, @Context UriInfo uriInfo) {
		if (inventoryService == null) {
			inventoryService = (InventoryService) ApplicationContextProvider.getApplicationContext()
					.getBean("inventoryService");
		}
		Car car = inventoryService.getCarDetails(carId);

		if(car == null) {
			Link link = getLinkForCarList(uriInfo);
			return "{\"Error\": \"Car not listed for sale\""
			     + ",\"uri\": \"" + link.getUri() + "\","
			     + "\"rel\": \"" + link.getRel() + "\""
			     + "}";
		}

		List<Link> links = new LinkedList<>();
		links.add(getLinkForBuyingCar(uriInfo, carId));
		links.add(getLinkForAddingCar(uriInfo));
		links.add(getLinkForCarList(uriInfo));
		car.setLinks(links);

		return new Gson().toJson(car);

	}

	private Link getLinkForBuyingCar(UriInfo uriInfo, int carId) {
		String uri = uriInfo.getBaseUriBuilder()
				.path(InventoryHandler.class)
				.path(InventoryHandler.class, "buyCar")
				.resolveTemplate("carId", carId).toString();
		String rel = "Buy this car";
		return new Link(uri,rel);
	}

	private Link getLinkForCarDetails(UriInfo uriInfo, int carId) {
		String uri = uriInfo.getBaseUriBuilder().path(getClass()).path(getClass(), "getCarDetails").resolveTemplate("carId", carId).toString();
		String rel = "See car details";
		return new Link(uri,rel);
	}

	private Link getLinkForCarList(UriInfo uriInfo) {
		String uri = uriInfo.getBaseUriBuilder()
				.path(InventoryHandler.class)
				.path(InventoryHandler.class, "listForSale")
				.toString();

		String rel = "See all Cars listed for sale";
		return new Link(uri,rel);
	}

	private Link getLinkForAddingCar(UriInfo uriInfo) {
		String uri = uriInfo.getBaseUriBuilder().path(getClass()).path(getClass(), "addForSale").toString();
		String rel = "Add a car for sale";
		return new Link(uri,rel);
	}
}
