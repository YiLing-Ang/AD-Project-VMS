package iss.team5.vms.controllers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import iss.team5.vms.helper.BookingStatus;
import iss.team5.vms.helper.Category;
import iss.team5.vms.helper.ReportStatus;
import iss.team5.vms.model.Booking;
import iss.team5.vms.model.Report;
import iss.team5.vms.model.Room;
import iss.team5.vms.model.Student;
import iss.team5.vms.repositories.StudentRepo;
import iss.team5.vms.services.BookingService;
import iss.team5.vms.services.FacilityService;
import iss.team5.vms.services.ReportService;
import iss.team5.vms.services.RoomService;
import iss.team5.vms.services.StudentService;
import iss.team5.vms.services.UserService;

@RestController
@RequestMapping(path = "api/student", produces = "application/json")
public class RestStudentController {

	@Autowired
	BookingService bs;

	@Autowired
	StudentService ss;

	@Autowired
	RoomService rms;

	@Autowired
	ReportService rs;

	@Autowired
	FacilityService fs;

	@Autowired
	UserService us;

	@Autowired
	StudentRepo srepo;

	@RequestMapping("/booking/history")
	public List<Booking> bookingHistory(Student student) {
		Student s1 = ss.findStudentById(3);
		List<Booking> bookings = bs.findBookingsByStudent(s1);
		return bs.checkBookingInProgress(bookings);
	}

	@PostMapping(value = "/booking/options")
	public List<Booking> bookingOptions(@RequestBody List<Map<String, Object>> rawPayload) {
		Map<String, Object> payload = rawPayload.get(0);

		LocalDate date = LocalDate.parse((String) payload.get("date"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		LocalTime time = LocalTime.parse((String) payload.get("time"));

		Booking booking = new Booking("1", date, time, 2, BookingStatus.WAITINGLIST);
		Room room = new Room("1", 2, fs.jsonToFacilityList((String) payload.get("facilities")));

		List<Room> rooms = rms.findRoomsByAttributes(room);
		List<Booking> bookings = bs.checkBookingAvailable(booking, rooms);

		System.out.println(payload.get("facilities"));
		System.out.println(payload.get("date"));
		System.out.println(payload.get("time"));
		System.out.println(payload.get("capacity"));

		return bookings;
	}

	@PostMapping(value = "/report/save")
	public String newReportAndroid(@RequestBody List<Map<String, Object>> rawPayload) {

		Map<String, Object> payload = rawPayload.get(0);

		String encodedString = (String) payload.get("image");
		byte[] encodedByte = Base64.decodeBase64(encodedString);

		String name = UUID.randomUUID().toString().replaceAll("-", "");
		String imageType = ".png";
		String filePath = "C:/VMS/img/";
		File outputFileFolder = new File(filePath);
		if (!outputFileFolder.exists()) {
			outputFileFolder.mkdirs();
		}
		try {
			FileUtils.writeByteArrayToFile(new File(filePath + name + imageType), encodedByte);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// add path to report
		Student student = ss.findStudentById((int) (payload.get("id")));
		Booking booking = bs.findStudentCurrentBooking(student);
		Booking lastBooking = bs.findLastBooking(booking);

		rs.createReport(new Report((String) payload.get("details"), name + imageType, lastBooking,
				ReportStatus.PROCESSING, Category.valueOf((String) payload.get("category"))));
		System.out.println("4 success");
		// test for getting real path for app
		System.out.println(filePath + name + imageType);// real path in local
		/*
		 * ms.sendSimpleMail("e0838388@u.nus.edu","report test","new report generated!"
		 * );
		 */
		System.out.println("report success");
		return "report-success";
	}

}
