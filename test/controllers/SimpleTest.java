package controllers;

import io.exercise.api.models.Dashboard;
import io.exercise.api.models.Roles;
import io.exercise.api.models.User;
import io.exercise.api.utils.DatabaseUtils;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

/**
 * Created by agonlohaj on 06 Oct, 2020
 */
public class SimpleTest extends WithApplication {

	String token = "bearer ";
	User user;

	@Override
	protected Application provideApplication() {
		return new GuiceApplicationBuilder().build();
	}

	/**
	 * Authenticates a test user and gets a token, then adds some dashboards.
	 */
	@Before
	public void init() {
		//Getting the token
		this.user = new User();
		user.setUsername("testingUser");
		user.setPassword("testingPass");
		user.setRoles(List.of("62e7dc19bcae4f4a49908dca"));

		//Adding the user to mongo via our route
		route(app, new Http.RequestBuilder()
			.method(POST)
			.uri("/api/user")
			.bodyJson(Json.toJson(user)));

		Http.RequestBuilder authenticateRequest = new Http.RequestBuilder()
			.method(POST)
			.uri("/api/authenticate")
			.bodyJson(Json.toJson(user));
		final Result authenticateResult = route(app, authenticateRequest);
		assertEquals(OK, authenticateResult.status());
		token += contentAsString(authenticateResult).substring(1, contentAsString(authenticateResult).length() - 1);

		//Reading the user from our database
		Http.RequestBuilder request = new Http.RequestBuilder()
			.method(GET)
			.uri("/api/user")
			.header("token", token);
		final Result allResult = route(app, request);
		this.user = DatabaseUtils.parseJsonListOfType(Json.parse(contentAsString(allResult)), User.class).get(0);

		//Adding dashboards
		Random rand = new Random();
		List<String> ids = List.of("", this.user.getId().toString(), Roles.Admin.getId().toString(), Roles.Member.getId().toString());
		AtomicInteger integer = new AtomicInteger(0);
		Stream.generate(() -> {
			Dashboard d = Dashboard.builder()
				.name("Dashboard " + integer.getAndIncrement())
				.readACL(List.of(ids.get(rand.nextInt(ids.size()))))
				.writeACL(List.of(ids.get(rand.nextInt(ids.size()))))
				.build();

			if (integer.get() == 1) {
				d.setId(new ObjectId("62e7dc19bcae4f4a49908dcc"));
				d.setWriteACL(List.of(this.user.getId().toString()));
			}
			return new Http.RequestBuilder()
				.method(POST)
				.uri("/api/dashboard")
				.bodyJson(Json.toJson(d))
				.header("token", token);
			})
			.limit(5)
			.forEach(req -> route(app, req));
	}

	/**
	 * Tests if index is working properly.
	 */
	@Test
	public void testIndex() {
		Http.RequestBuilder request = new Http.RequestBuilder()
			.method(GET)
			.uri("/");

		Result result = route(app, request);
		assertEquals(OK, result.status());
		assertTrue(contentAsString(result).contains("Welcome to Play!"));
	}

	/**
	 * Tests if authentication is working properly.
	 */
	@Test
	public void testAuthenticate() {
		User user = new User();
		user.setUsername("testingUser");
		user.setPassword("testingPass");

		Http.RequestBuilder request = new Http.RequestBuilder()
			.method(POST)
			.uri("/api/user")
			.bodyJson(Json.toJson(user));
		final Result result = route(app, request);

		assertEquals(OK, result.status());

		Http.RequestBuilder authenticateRequest = new Http.RequestBuilder()
			.method(POST)
			.uri("/api/authenticate")
			.bodyJson(Json.toJson(user));
		final Result authenticateResult = route(app, authenticateRequest);

		assertEquals(3, contentAsString(authenticateResult).split("\\.").length);
		assertEquals(OK, authenticateResult.status());
	}

	/**
	 * Tests if CRUD get (all) is working properly.
	 */
	@Test
	public void testAll() {
		Http.RequestBuilder request = new Http.RequestBuilder()
			.method(GET)
			.uri("/api/dashboard")
			.header("token", token);
		final Result allResult = route(app, request);
		assertEquals(OK, allResult.status());
	}

	/**
	 * Tests adding a dashboard.
	 */
	@Test
	public void testCreate() {
		Dashboard d1 = new Dashboard();
		d1.setName("Dashboard");
		d1.setReadACL(List.of(this.user.getId().toString(), "62e7dc19bcae4f4a49908dca"));
		d1.setWriteACL(List.of(this.user.getId().toString()));
		d1.setId(new ObjectId());

		Result result = route(app, new Http.RequestBuilder()
			.method(POST)
			.uri("/api/dashboard")
			.bodyJson(Json.toJson(d1))
			.header("token", token));

		assertEquals(Json.toJson(d1), Json.parse(contentAsString(result)));
		assertFalse(result.contentType().isEmpty());
		assertEquals("application/json", result.contentType().get());
		assertEquals(OK, result.status());
	}

	/**
	 * Tests adding a dashboard with an empty name.
	 */
	@Test
	public void testCreateEmpty() {
		Dashboard d1 = new Dashboard();
		d1.setReadACL(List.of(this.user.getId().toString(), "62e7dc19bcae4f4a49908dca"));
		d1.setWriteACL(List.of(this.user.getId().toString()));

		Result result = route(app, new Http.RequestBuilder()
			.method(POST)
			.uri("/api/dashboard")
			.bodyJson(Json.toJson(d1))
			.header("token", token));

		assertEquals(BAD_REQUEST, result.status());
	}

	/**
	 * Tests updating a dashboard.
	 */
	@Test
	public void testUpdate() {
		Dashboard d1 = new Dashboard();
		d1.setName("Updated dashboard");
		d1.setReadACL(List.of(this.user.getId().toString(), "62e7dc19bcae4f4a49908dca"));
		d1.setWriteACL(List.of(this.user.getId().toString()));

		Result result = route(app, new Http.RequestBuilder()
			.method(PUT)
			.uri("/api/dashboard/62e7dc19bcae4f4a49908dcc")
			.bodyJson(Json.toJson(d1))
			.header("token", token));

		Dashboard resultDashboard = Json.fromJson(Json.parse(contentAsString(result)), Dashboard.class);

		assertEquals(d1, resultDashboard);
		assertFalse(result.contentType().isEmpty());
		assertEquals("application/json", result.contentType().get());
		assertEquals(OK, result.status());
	}

	/**
	 * Tests deleting a dashboard.
	 */
	@Test
	public void testDelete() {
		Result result = route(app, new Http.RequestBuilder()
			.method(DELETE)
			.uri("/api/dashboard/62e7dc19bcae4f4a49908dcc")
			.header("token", token));

		Dashboard resultDashboard = Json.fromJson(Json.parse(contentAsString(result)), Dashboard.class);

		assertNotEquals(null, resultDashboard);
		assertFalse(result.contentType().isEmpty());
		assertEquals("application/json", result.contentType().get());
		assertEquals(OK, result.status());
	}
}
