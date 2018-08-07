package com.ashwin.RestDemo.service;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ashwin.RestDemo.model.Car;
import com.ashwin.RestDemo.model.Deal;
import com.ashwin.RestDemo.model.Person;
@Service("inventoryService")
public class InventoryService {
	@Autowired
	private SessionFactory sessionFactory;
	@Autowired
	private AuthenticationService authenticationService;

	public Car listForSale(Car car, String authCredentials) {
		Session session = null;
		String userName = authenticationService.getUserName(authCredentials);
		Person seller = new Person();
		seller.setUserName(userName);
		int id = 0;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			// seller = (Person) session.get(Person.class, userName);
			car.setOwner(seller);
			car.setForSale(true);
			id = (Integer) session.save(car);
			tx.commit();
		} catch (Exception exception) {
			tx.rollback();
			session.close();
			exception.printStackTrace();
			return null;
		} finally {
			session.close();
		}
		if (car != null)
			car.setId(id);
		return car;
	}

	public AuthenticationService getAuthenticationService() {
		return authenticationService;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public List<Car> listAllForSale() {
		Session session = null;
		List<Car> carList = null;
		try {
			session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			Query<Car> query = session.createQuery("from Car where forSale = true");
			carList = query.list();
			tx.commit();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			session.close();
		}
		return carList;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public Deal buy(int carId, String authCredentials) {

		String username = authenticationService.getUserName(authCredentials);
		Session session = null;
		Deal deal = null;
		Car car = new Car();
		Person buyer = new Person();
		buyer.setUserName(username);
		int id = 0;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			car = (Car) session.get(Car.class, carId);
			if (car.isForSale()) {
				deal = new Deal();
				deal.setSeller(car.getOwner());
				deal.setBuyer(buyer);
				car.setOwner(buyer);
				car.setForSale(false);
				deal.setCar(car);
				deal.setSoldOn(new Date());
				deal.setPrice(car.getPrice());
				id = (Integer) session.save(deal);
				tx.commit();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			tx.rollback();
			session.close();
			return null;
		} finally {
			session.close();
		}
		if (deal!=null) deal.setId(id);
		return deal;
	}

	public List<Car> listCarsSold(String authCredentials) {
		// TODO Auto-generated method stub
		String userName = authenticationService.getUserName(authCredentials);
		Session session = null;
		List<Car> list = null;
		try {
			session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			Query query = session.createQuery("select deal.car from Deal deal where deal.seller.userName = '" + userName + "'");
			list = query.list();
			tx.commit();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			session.close();
		}
		return list;
	}

	public Car getCarDetails(int carId) {
		// TODO Auto-generated method stub
		Session session = null;
		Car car = null;
		try {
			session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			car = (Car) session.get(Car.class, carId);
			tx.commit();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			session.close();
		}
		return car;
	}

	/*
	 * public Car updateCarDetails(int carId, String authCredentials, Car
	 * updatedCarDetails) { // TODO Auto-generated method stub String userName =
	 * authenticationService.getUserName(authCredentials); Session session; Car
	 * car; try { session = sessionFactory.openSession(); car = (Car)
	 * session.get(Car.class, carId); car. }
	 *
	 * return null; }
	 */
}
