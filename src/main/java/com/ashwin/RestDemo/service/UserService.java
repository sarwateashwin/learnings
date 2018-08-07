package com.ashwin.RestDemo.service;

import java.util.List;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ashwin.RestDemo.model.Person;
import com.ashwin.RestDemo.model.UserAccess;
import com.ashwin.RestDemo.model.UserRole;
@Service("userService")
public class UserService {

	public UserService() {
		// TODO Auto-generated constructor stub
	}
	@Autowired
	SessionFactory sessionFactory;
	@Autowired
	ActiveMQConnectionFactory mqConnectionFactory;
	public UserAccess addSeller(Person seller) {
		Session session = null;
		String password = null;
		UserAccess userAccess = null;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			Query<Person> query = session.createQuery("from Person where userName = '" + seller.getUserName() + "'");
			if(!query.list().isEmpty()) throw new Exception("Seller Already Exists");
			session.save(seller);
			userAccess = new UserAccess(seller, UserRole.SELLER);
			int userId = (Integer) session.save(userAccess);
			password = seller.getUserName() + "_" + userId;
			userAccess.setPassword(password);
			tx.commit();
		} catch (Exception exception) {
			exception.printStackTrace();
			tx.rollback();
			userAccess = null;
		} finally {
			session.close();
		}
		try {
			Connection mqConnection = mqConnectionFactory.createConnection();
			mqConnection.start();
			javax.jms.Session mqSession = mqConnection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
			Destination topic = mqSession.createTopic("addition.users");
			MessageProducer messageSender = mqSession.createProducer(topic);
			//messageSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			//ObjectMessage message = mqSession.createObjectMessage();
			//message.setObject(userAccess);
			messageSender.send(mqSession.createTextMessage("Created!"));
			mqSession.close();
			mqConnection.close();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return userAccess;
	}

	public UserAccess addBuyer(Person buyer) {
		Session session = null;
		String password = null;
		UserAccess userAccess = null;
		Transaction tx = null;
		try {
			session = sessionFactory.openSession();
			tx = session.beginTransaction();
			Query<Person> query = session.createQuery("from Person where userName = '" + buyer.getUserName() + "'");
			if(!query.list().isEmpty()) throw new Exception("Buyer Already Exists");
			session.save(buyer);
			userAccess = new UserAccess(buyer, UserRole.BUYER);
			int userId = (Integer) session.save(userAccess);
			password = buyer.getUserName() + "_" + userId;
			userAccess.setPassword(password);
			tx.commit();
		} catch (Exception exception) {
			exception.printStackTrace();
			tx.rollback();
			userAccess = null;
		} finally {
			session.close();
		}
		return userAccess;
	}

	public List<Person> listAllUsers() {
		// TODO Auto-generated method stub
		Session session = null;
		List<Person> list = null;
		try {
			session = sessionFactory.openSession();
			Transaction tx = session.beginTransaction();
			Query query = session.createQuery("from Person");
			list = query.list();
			tx.commit();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			session.close();
		}
		return list;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public UserAccess getUserDetails(String userName) {
		// TODO Auto-generated method stub

		UserAccess userAccess = null;
		Session session = null;
		try {
			session = sessionFactory.openSession();
			Query<UserAccess> query = session.createQuery("from UserAccess where user.userName = '" + userName +"'");
			List<UserAccess> list = query.list();
			userAccess = list.get(0);
		} catch (Exception exception) {
			exception.printStackTrace();
			session.close();
			return null;
		} finally {
			session.close();
		}

		return userAccess;
	}

}
