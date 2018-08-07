package com.ashwin.RestDemo.restapi;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.ashwin.RestDemo.config.ApplicationContextProvider;
import com.ashwin.RestDemo.model.Link;
import com.ashwin.RestDemo.model.Person;
import com.ashwin.RestDemo.model.UserAccess;
import com.ashwin.RestDemo.service.UserService;
import com.google.gson.Gson;

/**
 * Root resource (exposed at "users" path)
 */
@Path("users")
public class MyResource {

	private UserService userService;

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to
	 * the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@RolesAllowed("ADMIN")
	@Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
	// @Path("/users")
	public List<Person> listAllUsers(@Context UriInfo uriInfo) {
		if (userService == null) {
			userService = (UserService) ApplicationContextProvider.getApplicationContext().getBean("userService");
		}
		List<Person> userList = userService.listAllUsers();
		List<Link> links;

		if (userList == null || userList.isEmpty()) {
			Link buyerLink = getLinkForAddingBuyer(uriInfo);
			Link sellerLink = getLinkForAddingSeller(uriInfo);
			/*return "{\"Error\": \"No users registered!\"," + "\"links\":[{\"uri\": \"" + buyerLink.getUri() + "\"" + ",\"rel\": \""
					+ buyerLink.getRel() + "\"}," + "{\"uri\": \"" + sellerLink.getUri() + "\"" + ",\"rel\": \""
					+ sellerLink.getRel() + "\"}]}";*/
		}

		for (Person user : userList) {
			links = new LinkedList<>();
			links.add(getLinkForUserDetails(uriInfo, user.getUserName()));
			links.add(getLinkForAddingBuyer(uriInfo));
			links.add(getLinkForAddingSeller(uriInfo));
			user.setLinks(links);
		}

		//return new Gson().toJson(userList);
		return userList;
	}

	@GET
	@RolesAllowed("ADMIN")
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{userName}")
	public String getUserDetails(@PathParam("userName") String userName, @Context UriInfo uriInfo) {
		if (userService == null) {
			userService = (UserService) ApplicationContextProvider.getApplicationContext().getBean("userService");
		}
		UserAccess userAccess = userService.getUserDetails(userName);
		if (userAccess == null) {
			Link link = getLinkForUserList(uriInfo);
			return "{\"Error\":\"No user registered with this username!\"," + "\"uri\": \"" + link.getUri() + "\"" + ",\"rel\": \""
					+ link.getRel() + "\"" + "}";
		}
		List<Link> links = new LinkedList<>();

		links.add(getLinkForUserList(uriInfo));
		links.add(getLinkForAddingBuyer(uriInfo));
		links.add(getLinkForAddingSeller(uriInfo));
		userAccess.setLinks(links);

		return new Gson().toJson(userAccess);

	}

	@POST
	@RolesAllowed("ADMIN")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/addSeller")
	public String addSeller(Person seller, @Context UriInfo uriInfo) {
		Gson gson = new Gson();
		//Person person = gson.fromJson(json, Person.class);
		if (userService == null) {
			userService = (UserService) ApplicationContextProvider.getApplicationContext().getBean("userService");
		}
		UserAccess userAccess = userService.addSeller(seller);
		if (userAccess == null) {
			Link link = getLinkForUserList(uriInfo);
			return "{\"Error\": \"Could not add Seller\"," + "\"uri\": \"" + link.getUri() + "\"" + ",\"rel\": \""
					+ link.getRel() + "\"" + "}";
		}
		List<Link> links = new LinkedList<>();
		links.add(getLinkForUserList(uriInfo));
		links.add(getLinkForAddingBuyer(uriInfo));
		userAccess.setLinks(links);

		return gson.toJson(userAccess);

	}

	@POST
	@RolesAllowed("ADMIN")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/addBuyer")
	public String addBuyer(Person buyer, @Context UriInfo uriInfo) {
		Gson gson = new Gson();
		//Person person = gson.fromJson(json, Person.class);
		if (userService == null) {
			userService = (UserService) ApplicationContextProvider.getApplicationContext().getBean("userService");
		}
		UserAccess userAccess = userService.addBuyer(buyer);
		if (userAccess == null) {
			Link link = getLinkForUserList(uriInfo);
			return "{\"Error\": \"Could not add Buyer\"," + "\"uri\": \"" + link.getUri() + "\"" + ",\"rel\": \""
					+ link.getRel() + "\"" + "}";

		}
		List<Link> links = new LinkedList<>();
		links.add(getLinkForUserList(uriInfo));
		links.add(getLinkForAddingSeller(uriInfo));
		userAccess.setLinks(links);

		return gson.toJson(userAccess);
	}

	private Link getLinkForUserList(UriInfo uriInfo) {
		String uri = uriInfo.getBaseUriBuilder().path(getClass()).toString();
		String rel = "See all registered users";
		return new Link(uri, rel);
	}

	private Link getLinkForUserDetails(UriInfo uriInfo, String userName) {
		String uri = uriInfo.getBaseUriBuilder().path(getClass()).path(getClass(), "getUserDetails")
				.resolveTemplate("userName", userName).toString();
		String rel = "Get access details for this user.";
		return new Link(uri, rel);
	}

	private Link getLinkForAddingBuyer(UriInfo uriInfo) {
		String uri = uriInfo.getBaseUriBuilder().path(getClass()).path(getClass(), "addBuyer").toString();
		String rel = "Add a Buyer";
		return new Link(uri, rel);
	}

	private Link getLinkForAddingSeller(UriInfo uriInfo) {
		String uri = uriInfo.getBaseUriBuilder().path(getClass()).path(getClass(), "addSeller").toString();
		String rel = "Add a Seller";
		return new Link(uri, rel);
	}
}