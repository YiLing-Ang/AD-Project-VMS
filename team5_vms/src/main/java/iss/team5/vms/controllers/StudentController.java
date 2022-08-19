package iss.team5.vms.controllers;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import iss.team5.vms.DTO.Account;
import iss.team5.vms.helper.BookingStatus;
import iss.team5.vms.helper.ReportCategory;
import iss.team5.vms.helper.ReportStatus;
import iss.team5.vms.model.Booking;
import iss.team5.vms.model.Facility;
import iss.team5.vms.model.Report;
import iss.team5.vms.model.Room;
import iss.team5.vms.model.Student;
import iss.team5.vms.model.User;
import iss.team5.vms.repositories.StudentRepo;
import iss.team5.vms.services.AccountAuthenticateService;
import iss.team5.vms.services.BookingService;
import iss.team5.vms.services.FacilityService;
import iss.team5.vms.services.MailService;
import iss.team5.vms.services.ReportService;
import iss.team5.vms.services.RoomService;
import iss.team5.vms.services.StudentService;
import iss.team5.vms.services.UserService;
import iss.team5.vms.services.UserSessionService;

@Controller
@RequestMapping("/student")
public class StudentController {

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
	MailService ms;

	@Autowired
	HttpSession session;

	@Autowired
	private UserSessionService userSessionService;

	@Autowired
	private AccountAuthenticateService accAuthService;

	@RequestMapping("/home")
	public ModelAndView studentHome(HttpServletRequest request) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}
		List<Room> rooms = rms.findAllRooms();
		Booking bookingForTheDay = new Booking();
		bookingForTheDay.setDate(LocalDate.now());
		bookingForTheDay.setTime(LocalTime.now());

		// String username =
		// SecurityContextHolder.getContext().getAuthentication().getName();
		// Student s1 = ss.findStudentByUser(us.findUserByUsername(username));

		HttpSession session = request.getSession();
		Student student = (Student) session.getAttribute("student");
		List<Booking> availableBookings = bs.findBookingsAvailableExact(bookingForTheDay, rooms, student);
		List<Booking> studentBookingToday = bs.findStudentBookingsForDate(student, LocalDate.now());
		Stack<Booking> stackBookings = new Stack<Booking>();
		stackBookings.addAll(availableBookings);
		System.out.println(stackBookings.size());
//		List<Booking> findTodayBooking=sBooking.stream()
//		.filter(b-> b.getDate()==LocalDate.now() && b.getStatus().toString().equalsIgnoreCase("SUCCESSFUL") )
//		.collect(Collectors.toList());

//		ModelAndView mav = new ModelAndView("student-home-page");
//		try  { 
//			
//			Booking bookingOfTheDay = bs.findStudentCurrentBooking(student);
//			
//			mav.addObject("bookingOfTheDay", bookingOfTheDay);
//			String strRoomName = "Room Name :  " + bookingOfTheDay.getRoom().getRoomName();
//			
//			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//			String bookingDate = bookingOfTheDay.getDate().format(dateFormatter);
//			DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
//			String bookingTime = bookingOfTheDay.getTime().format(timeFormatter);	
//			String strDateTime = "Date/Time :  " + bookingDate +" "+bookingTime;
//			String strDuration = "Duration  :  " + bookingOfTheDay.getDuration() + " minutes";
////			String strBookingInProgress = "Checked-In:  In Progress";
//			
//			mav.addObject("strRoomName",strRoomName);
//			mav.addObject("strDateTime",strDateTime);
//			mav.addObject("strDuration",strDuration);
//			mav.addObject("BookingInProgress",bookingOfTheDay.isBookingInProgress());
//			mav.addObject("CheckIn",bookingOfTheDay.isCheckedIn());
//			System.out.println("print 1");
//			
//		} catch (Exception e) {
//			try {
//				
//				Booking nextBookingOfTheDay = bs.findStudentNextBooking(student);
//				
//				mav.addObject("bookingOfTheDay", nextBookingOfTheDay);
//				String strRoomName = "Room Name :  " + nextBookingOfTheDay.getRoom().getRoomName();
//				
//				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//				String bookingDate = nextBookingOfTheDay.getDate().format(dateFormatter);
//				DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
//				String bookingTime = nextBookingOfTheDay.getTime().format(timeFormatter);	
//				String strDateTime = "Date/Time :  " + bookingDate +" "+bookingTime;
//				String strDuration = "Duration  :  " + nextBookingOfTheDay.getDuration() + " minutes";
////				String strBookingInProgress = "Checked-In:  In Progress";
//				
//				mav.addObject("strRoomName",strRoomName);
//				mav.addObject("strDateTime",strDateTime);
//				mav.addObject("strDuration",strDuration);
//				mav.addObject("BookingInProgress",nextBookingOfTheDay.isBookingInProgress());
//				
////				String strBkInProgress = "Checked-In:  Under waiting list";
//				System.out.println(nextBookingOfTheDay.isBookingInProgress());
//				mav.addObject("CheckIn",nextBookingOfTheDay.isCheckedIn());
//				System.out.println("print 2");
//			}
//			catch (Exception e2) {
//				Booking noBooking = new Booking();
//				mav.addObject("noBooking",noBooking);
//				System.out.println("print 3");
//			}
//		} 
//		ModelAndView mav = new ModelAndView("student-home-page");
//		if (studentBookingToday.size() > 0) {
////			Booking bookingOfTheDay = studentBookingToday.get(0);
////			ModelAndView mav = new ModelAndView("student-home-page");
//			mav.addObject("bookingOfTheDay", studentBookingToday.get(0));
//		}

		ModelAndView mav = new ModelAndView("student-home-page");
		try {
			Booking booking = bs.findStudentCurrentBooking(student);
			mav.addObject("booking", booking);
			mav.addObject("current", true);
		} catch (Exception e) {
			try {
				Booking booking = bs.findStudentNextBooking(student);
				mav.addObject("booking", booking);
				mav.addObject("current", false);
			} catch (Exception e2) {
			}
		}
		List<List<Booking>> bookingsForCarousel = new ArrayList<List<Booking>>();
		for (int i = availableBookings.size(); i > 0; i = i - 3) {
			List<Booking> bookings = new ArrayList<Booking>();
			for (int k = 0; k < 3; k++) {
				if (!stackBookings.isEmpty()) {
					bookings.add(stackBookings.pop());
				}
			}
			bookingsForCarousel.add(bookings);
		}

//		System.out.println(bookingsForCarousel.size());
		mav.addObject("bookings", availableBookings);
		if (bookingsForCarousel.size() > 0) {
			mav.addObject("bookingsCarousel1", bookingsForCarousel.get(0));
			bookingsForCarousel.remove(0);
		}
		System.out.println(bookingsForCarousel.size());
		mav.addObject("bookingsCarousel", bookingsForCarousel);
		return mav;
	}

//	@RequestMapping("/checkin/{bookingId}/{studentId}")
//	public ModelAndView bookingCheckin(@PathVariable("bookingId") String bookingId, @PathVariable("studentId") String studentId) {
//		User user = userSessionService.findUserBySession();
//		if(!user.getRole().equals("STUDENT")) {
//			ModelAndView mav = new ModelAndView("unauthorized-admin");
//			return mav;
//		}
//		// pending login implementation
//		// hardcoded student object for now, final implementation should retrieve from
//		// logged in context
//		//String username = SecurityContextHolder.getContext().getAuthentication().getName();
//		//Student student = ss.findStudentByUser(us.findUserByUsername(username));
//		//Student student = ss.findStudentById("S00001");
//		Student student = ss.findStudentById(studentId);	
//		// pending proper url to be forwarded to on check-in completion
//		ModelAndView mav = new ModelAndView("student-bookings-list");
//		Booking booking = bs.findBookingById(bookingId);
//		String outcomeMsg = "";
//		if (booking == null) {
//			outcomeMsg = "Error: booking not found";
//		} else {
//			outcomeMsg = bs.checkIn(student, booking);
//		}
//		mav.addObject("outcomeMsg", outcomeMsg);
//		return mav;
//	}

	@RequestMapping(value = "/booking/options", method = RequestMethod.GET)
	public ModelAndView bookingOptionSelection() {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}
		Booking booking = new Booking();
		Room room = new Room();
		ModelAndView mav = new ModelAndView("student-bookings-filter_selection");
		List<Facility> facilities = fs.findAllFacilities();
		mav.addObject("fList", facilities);
		mav.addObject("booking", booking);
		mav.addObject("room", room);
		mav.addObject("size", user.getGroupSize());
		return mav;
	}

	@RequestMapping(value = "/booking/options", method = RequestMethod.POST)
	public ModelAndView bookingOptionSelected(Booking booking, Room room, @RequestParam("date") LocalDate date) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}

		LocalTime currentTime = LocalTime.now();
		LocalTime hr_min = LocalTime.of(currentTime.getHour(), currentTime.getMinute());

		ModelAndView mav = new ModelAndView("student-bookings-filter_selection");
		List<Facility> facilities = fs.findAllFacilities();
		mav.addObject("fList", facilities);
		mav.addObject("booking", booking);
		mav.addObject("room", room);
		mav.addObject("size", user.getGroupSize());

		LocalDate now = LocalDate.now();
		if (date.isBefore(now)) {
			mav.addObject("yesterday", true);
			mav.addObject("past", false);
			return mav;
		} else if (date.isAfter(now.plusDays(14))) {
			mav.addObject("max", true);
			mav.addObject("past", false);
			return mav;
		} else if (isWeekend(date)) {
			mav.addObject("weekend", true);
			mav.addObject("past", false);
			return mav;
		} else if (date.isEqual(now) && booking.getTime().isBefore(hr_min.minusMinutes(1))) {
			mav.addObject("past", true);
			return mav;
		}

		Student student = ss.findStudentByUser(user);
		List<Room> roomsExact = rms.findRoomsByExactAttributes(room);
		List<Booking> bookings = new ArrayList<Booking>();
		bookings = bs.findBookingsAvailableExact(booking, roomsExact, student);
		if (bookings.size() == 0) {
//			System.out.println("Alternative list");
			List<Room> roomsContaining = rms.findRoomsByContainingAttributes(room);
			bookings.addAll(bs.findBookingsAvailableAlternative(booking, roomsExact, student));
			bookings.addAll(bs.findBookingsAvailableExact(booking, roomsContaining, student));
		}

		ModelAndView mav1 = new ModelAndView("student-bookings-slot_selection");
		mav1.addObject("bookings", bookings);
		mav1.addObject("room", room);
		return mav1;
	}

	public static boolean isWeekend(final LocalDate ld) {
		DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
		return day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY;
	}

	@RequestMapping(value = "/booking/save", method = RequestMethod.POST)
	public Object bookingNew(Booking booking, @RequestParam("roomid") String roomString, HttpServletRequest request) {

		Student student = (Student) session.getAttribute("student");
		Room room = rms.findRoomById(roomString);

		booking.setStudent(student);
		booking.setRoom(room);

		if (bs.checkBookingByDateTimeRoom(booking, room)) {
			booking.setStatus(BookingStatus.REJECTED);
			ModelAndView mav = new ModelAndView("booking-success");
			mav.addObject("booking", booking);
			return mav;
		} else {
//			booking.setRoom(rs.findRoomById(room.getId()));
			if (booking.getStudent().getScore() >= 3) {
				booking.setStatus(BookingStatus.WAITINGLIST);
				bs.scheduleWaitingList(booking, room);
				bs.createBooking(booking);
				return "forward:/student/booking/status/" + booking.getId();
			} else {
				booking.setStatus(BookingStatus.SUCCESSFUL);
				bs.createBooking(booking);
				return "forward:/student/booking/status/" + booking.getId();
			}
//			return "error";	
		}
	}

	@RequestMapping(value = "booking/status/{bookingId}")
	public ModelAndView bookingStatus(@PathVariable("bookingId") String bookingId) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}
		ModelAndView mav = new ModelAndView("booking-success");
		Booking booking = bs.findBookingById(bookingId);
		mav.addObject("booking", booking);
		return mav;
	}

//	//this is for report form test
//	@RequestMapping("/reportform")
//	public String reportform(){
//		System.out.println("0 success");
//		return "misuse-report-form";
//	}

	@RequestMapping("/booking/history")
	public ModelAndView bookingHistory() {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}

		Student student = (Student) session.getAttribute("student");

		List<Booking> bookings = bs.findBookingsByStudent(student);
		List<Booking> bookings2 = bs.updateBookingInProgress(bookings);
		ModelAndView mav = new ModelAndView("student-bookings-list");
		mav.addObject("bookings", bookings2);
		LocalDate datenow = LocalDate.now();
		LocalTime timenow = LocalTime.now();
		mav.addObject("datenow", datenow);
		mav.addObject("timenow", timenow);

		return mav;
	}

	@RequestMapping(value = "/booking/report", method = RequestMethod.GET)
	public ModelAndView bookingReport() {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}
		Student student = ss.findStudentByUser(user);
		Booking booking = new Booking();
		try {
			booking = bs.findStudentCurrentBooking(student);
		} catch (NoSuchElementException e) {
			ModelAndView mav = new ModelAndView("booking-not-inprogress");
			return mav;
		}
		Report report = new Report();
		ModelAndView mav = new ModelAndView("misuse-report-form");
		mav.addObject("report", report);
		mav.addObject("booking", booking);
		return mav;
	}

	@RequestMapping(value = "/booking/cancel/{bookingId}", method = RequestMethod.GET)
	public String cancelBooking(@PathVariable String bookingId) {
		Booking booking = bs.findBookingById(bookingId);
		booking.setStatus(BookingStatus.CANCELLED);
		bs.createBooking(booking);
		Room room = booking.getRoom();
		room.setBlockDuration(0);
		room.setBlockedStartTime(null);

		return "forward:/student/booking/history";
	}

	@RequestMapping(value = "/booking/extend", method = RequestMethod.GET)
	public ModelAndView extendBooking() {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}

		ModelAndView mav = new ModelAndView("booking-success");
		Student student = ss.findStudentByUser(user);
		Booking currentBooking = new Booking();
		try {
			currentBooking = bs.findStudentCurrentBooking(student);
		} catch (NoSuchElementException e) {
			mav = new ModelAndView("booking-not-inprogress");
			return mav;
		}
		String outcomeMsg = "";
		Booking extendBooking = new Booking("placeholder", currentBooking.getDate(),
				currentBooking.getTime().plusMinutes(currentBooking.getDuration()), 1, currentBooking.getRoom());
		// check if current booking is less than 1 hour before end
		if (LocalTime.now()
				.isAfter(currentBooking.getTime().plusMinutes(currentBooking.getDuration()).minusMinutes(60))) {
			// check if extension request clashes with next booking
			if (!bs.checkBookingByDateTimeRoom(extendBooking, currentBooking.getRoom())) {
				outcomeMsg = "APPROVED";
				currentBooking.setDuration(currentBooking.getDuration() + 60);
				bs.createBooking(currentBooking);
			} else {
				Booking overlapBooking = bs.findOverlapBookingByDateTimeRoom(extendBooking, currentBooking.getRoom());
				int newDuration = Math.toIntExact(currentBooking.getTime().plusMinutes(currentBooking.getDuration())
						.until(overlapBooking.getTime(), ChronoUnit.MINUTES));
				if (newDuration > 10) {
					outcomeMsg = "APPROVED - EXTENDED BY " + newDuration;
					currentBooking.setDuration(currentBooking.getDuration() + newDuration);
					bs.createBooking(currentBooking);
				} else {
					outcomeMsg = "DENIED - NEXT BOOKING < 10 MINUTES AFTER CURRENT";
				}
			}
		} else {
			outcomeMsg = "DENIED - EXTENSION REQUEST TO BE MADE <1 HOUR BEFORE END OF CURRENT BOOKING";
		}
		mav.addObject("booking", currentBooking);
		mav.addObject("outcomeMsg", outcomeMsg);
		return mav;
//		}
//		catch(Exception e) {
//			
//		}
	}

	@RequestMapping(value = "report/save", method = RequestMethod.POST)
	private String createReport(@RequestParam(value = "file", required = true) MultipartFile file,
			@RequestParam(value = "details", required = true) String details, HttpServletRequest request)
			throws IOException {
		String path = "";
		String fileName = "";
		if (!file.isEmpty()) {
			String name = UUID.randomUUID().toString().replaceAll("-", "");
			String imageType = file.getContentType();
			String suffix = imageType.substring(imageType.indexOf("/") + 1);
			File file1 = new File("");
			String filePath = file1.getCanonicalPath();// get app real path in local
			fileName = name + "." + suffix;
			File file2 = new File("C:/VMS/img");
			if (!file2.exists()) {
				file2.mkdirs();
			}
			path = "C:/VMS/img/" + fileName;
			file.transferTo(new File(path));
		}

		// add path to report
		// method to extract student from logged in session
		Student student = (Student) session.getAttribute("student");
		Booking booking = bs.findStudentCurrentBooking(student);
		Booking lastBooking = bs.findBookingBefore(booking);
		Report report = rs.createReport(new Report(details, fileName, lastBooking, ReportStatus.PROCESSING,
				ReportCategory.CLEANLINESS, student));
		if (report.getCategory().equals(ReportCategory.HOGGING)) {
			if (bs.predictHogging(path)) {
				rs.approveReportScoring(report);
			}
			else
			{
				report.setReportStatus(ReportStatus.REJECTED);
				rs.createReport(report);
			}
		}

		return "report-success";

	}

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public ModelAndView viewProfile() {
		User user = userSessionService.findUserBySession();
		Student stu = ss.findStudentByUser(user);
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}
		ModelAndView mav = new ModelAndView("student-profile");
		mav.addObject("student", stu);
		return mav;
	}

	@RequestMapping(value = "/profile/edit/{id}", method = RequestMethod.GET)
	public ModelAndView editProfile(@PathVariable String id) {
		User user = userSessionService.findUserBySession();
		Student stu = ss.findStudentById(id);
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}
		ModelAndView mav = new ModelAndView("student-profile-edit");
		mav.addObject("student", stu);
		return mav;
	}

	@RequestMapping(value = "/profile/edit", method = RequestMethod.POST)
	public ModelAndView saveProfile(@ModelAttribute @Valid Student student, BindingResult result) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}
		if (result.hasErrors())
			return new ModelAndView("student-profile-edit");
		Student stu = ss.updateStudent(student);
		ModelAndView mav = new ModelAndView("student-profile");
		mav.addObject("student", stu);
		return mav;
	}

	@RequestMapping(value = "/change-password/{id}", method = RequestMethod.GET)
	public ModelAndView changePassword(@PathVariable String id) {
		User user = userSessionService.findUserBySession();
		Student stu = ss.findStudentById(id);
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}
		ModelAndView mav = new ModelAndView("student-reset-password");
		mav.addObject("student", stu);
		return mav;
	}

	@RequestMapping(value = "/change-password", method = RequestMethod.POST)
	public ModelAndView changePassword(@ModelAttribute @Valid Student student,
			@RequestParam("oldPassword") String oldPassword, @RequestParam("password") String newPassword,
			@RequestParam("confirmPassword") String confirmPassword, BindingResult result) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("STUDENT")) {
			ModelAndView mav = new ModelAndView("unauthorized-admin");
			return mav;
		}

		Account acc = new Account(user.getGroupName(), oldPassword);
		User authUser = accAuthService.authenticateAccount(acc);

		if (authUser == null) {
			ModelAndView mav = new ModelAndView("student-reset-password");
			mav.addObject("error", true);
			return mav;
		}
		if (!(newPassword.equals(confirmPassword))) {
			ModelAndView mav = new ModelAndView("student-reset-password");
			mav.addObject("notMatch", true);
			return mav;
		}

		if (result.hasErrors())
			return new ModelAndView("student-reset-password");

		Student stu = ss.changePassword(student, newPassword);
		ModelAndView mav = new ModelAndView("student-profile");
		mav.addObject("student", stu);
		return mav;
	}

}