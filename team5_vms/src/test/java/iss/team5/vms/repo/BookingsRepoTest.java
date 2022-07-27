package iss.team5.vms.repo;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import iss.team5.vms.Team5VmsApplication;
import iss.team5.vms.helper.BookingStatus;
import iss.team5.vms.helper.dateTimeInput;
import iss.team5.vms.model.Booking;
import iss.team5.vms.repositories.BookingRepo;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Team5VmsApplication.class)
@TestMethodOrder(OrderAnnotation.class)
@AutoConfigureTestDatabase(replace =
AutoConfigureTestDatabase.Replace.NONE)

public class BookingsRepoTest {

	@Autowired BookingRepo br;
	
	@Test
    @Order(1)
	public void testCreateBooking()  {
    	List<Booking> bookingList = new ArrayList<Booking>();
    	bookingList.add(new Booking("B1", dateTimeInput.dateInput("01/01/2022"), LocalTime.now(), 1));
    	bookingList.add(new Booking("B2", dateTimeInput.dateInput("01/01/2022"), LocalTime.now(), 1));
    	bookingList.add(new Booking("B3", dateTimeInput.dateInput("01/01/2022"), LocalTime.now(), 1));
		br.saveAllAndFlush(bookingList);
		Booking b1 = bookingList.get(0);
		Assertions.assertNotNull(b1.getId());
    }
}