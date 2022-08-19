package iss.team5.vms.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import iss.team5.vms.helper.BookingStatus;
import iss.team5.vms.helper.FirstDayOfCurrentWeek;
import iss.team5.vms.helper.ReportCategory;
import iss.team5.vms.helper.ReportStatus;
import iss.team5.vms.model.Booking;
import iss.team5.vms.model.Facility;
import iss.team5.vms.model.Report;
import iss.team5.vms.model.Room;
import iss.team5.vms.model.Student;
import iss.team5.vms.model.User;
import iss.team5.vms.repositories.BookingRepo;
import iss.team5.vms.services.BookingService;
import iss.team5.vms.services.FacilityService;
import iss.team5.vms.services.MailService;
import iss.team5.vms.services.ReportService;
import iss.team5.vms.services.RoomService;
import iss.team5.vms.services.StudentService;
import iss.team5.vms.services.UserSessionService;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {

	@Autowired
	private RoomService rService;

	@Autowired
	private FacilityService fService;

	@Autowired
	private StudentService sService;

	@Autowired
	private BookingService bService;

	@Autowired
	private UserSessionService userSessionService;

	@Autowired
	private MailService mService;
	
	@Autowired
	private ReportService ReService;
	
	@Autowired
	private BookingRepo br;

	@RequestMapping(value = "/rooms/create", method = RequestMethod.GET)
	public ModelAndView newRoom() {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		ModelAndView mav = new ModelAndView("room-form");
		mav.addObject("room", new Room());
		List<Facility> facilities = (List<Facility>) fService.findAllFacilities();
		mav.addObject("fList", facilities);
		return mav;
	}

	@RequestMapping(value = "/rooms/create", method = RequestMethod.POST)
	public ModelAndView createRoom(@ModelAttribute @Valid Room room, BindingResult result, @RequestParam("roomName") String roomName) {
		User user = userSessionService.findUserBySession();
		List<Room> rooms = rService.findAllRooms();
		HashSet<String> names = new HashSet<>();
		for (Room r : rooms) {
			names.add(r.getRoomName());
		}
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		if (result.hasErrors())
			return new ModelAndView("room-form");
		for(String name : names) {
			if(name.equalsIgnoreCase(roomName)) {
				ModelAndView mav = new ModelAndView("room-form");
				mav.addObject("taken", true);
				return mav;
			}
		}
		ModelAndView mav = new ModelAndView("forward:/admin/rooms/list");
		room.setRoomName(roomName);
		rService.createRoom(room);
		return mav;
	}

	@RequestMapping(value = "/rooms/search", method = RequestMethod.GET)
	public ModelAndView searchRoom(@RequestParam("searchRoom") String roomName, @RequestParam("facility") String facStr,
			@RequestParam("availability") String ava, Model model) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		ModelAndView mav = new ModelAndView("rooms");

		List<Booking> bookings = bService.findAllBookings();
		HashSet<String> roomIds = new HashSet<>();
		for (Booking b : bookings) {
			roomIds.add(b.getRoom().getId());
		}
		List<Room> rooms = rService.findAllRooms();
		mav.addObject("rooms", rooms);
		mav.addObject("roomids", roomIds);

		List<Facility> facilities = (List<Facility>) fService.findAllFacilities();
		mav.addObject("checkBoxFacilities", facilities);
		mav.addObject("searchStr", roomName);
		System.out.println("Facilities" + facStr);
		if (roomName != "") {
			mav.addObject("rooms",
					rService.searchRoomByNameFacilityAvailability(roomName, facStr, Boolean.parseBoolean(ava)));
		} else {
			mav.addObject("rooms", rService.searchRoomByFacilityAvailability(facStr, Boolean.parseBoolean(ava)));
		}
		return mav;
	}

	@RequestMapping(value = "/rooms/list")
	@ResponseBody
	public ModelAndView roomList() {
		User user = userSessionService.findUserBySession();
		List<Booking> bookings = bService.findAllBookings();
		HashSet<String> roomIds = new HashSet<>();
		for (Booking b : bookings) {
			roomIds.add(b.getRoom().getId());
		}
		if (user != null) {
			if (!user.getRole().equals("ADMIN")) {
				ModelAndView mav = new ModelAndView("unauthorized-student");
				return mav;
			}
		}
		ModelAndView mav = new ModelAndView("rooms");
		List<Room> rooms = rService.findAllRooms();
		mav.addObject("rooms", rooms);
		List<Facility> facilities = (List<Facility>) fService.findAllFacilities();
		mav.addObject("checkBoxFacilities", facilities);
		mav.addObject("roomids", roomIds);
		return mav;
	}

	@RequestMapping(value = "/rooms/edit/{id}", method = RequestMethod.GET)
	public ModelAndView editRoom(@PathVariable String id) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		ModelAndView mav = new ModelAndView("room-edit");
		Room room = rService.findRoomById(id);
		mav.addObject("room", room);
		List<Facility> facilities = (List<Facility>) fService.findAllFacilities();
		mav.addObject("fList", facilities);
		List<Boolean> availabilities = new ArrayList<>();
		availabilities.add(Boolean.TRUE);
		availabilities.add(Boolean.FALSE);
		mav.addObject("availabilities", availabilities);
		return mav;
	}

	@RequestMapping(value = "/rooms/edit", method = RequestMethod.POST)
	public ModelAndView editRoom(@ModelAttribute @Valid Room room, BindingResult result) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		if (result.hasErrors())
			return new ModelAndView("room-edit");
		ModelAndView mav = new ModelAndView("rooms");
		rService.changeRoom(room);
		List<Room> rooms = rService.findAllRooms();
		List<Facility> facilities = (List<Facility>) fService.findAllFacilities();
		mav.addObject("checkBoxFacilities", facilities);
		mav.addObject("rooms", rooms);
		return mav;
	}

	@RequestMapping(value = "/rooms/delete/{id}")
	public ModelAndView deleteRoom(@PathVariable("id") String id) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		ModelAndView mav = new ModelAndView("forward:/admin/rooms/list");
		Room room = rService.findRoomById(id);
		rService.removeRoom(room);
		return mav;
	}

	@RequestMapping(value = "/students/create", method = RequestMethod.GET)
	public ModelAndView newStudent() {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		ModelAndView mav = new ModelAndView("student-form");
		mav.addObject("student", new Student());
		return mav;
	}

	@RequestMapping(value = "/students/create", method = RequestMethod.POST)
	public ModelAndView createStudent(@ModelAttribute @Valid Student student, BindingResult result, @RequestParam("groupName") String groupName, @RequestParam("email") String email) {
		User user = userSessionService.findUserBySession();
		List<Student> students = sService.findAllStudents();
		HashSet<String> names = new HashSet<>();
		HashSet<String> emails = new HashSet<>();
		for (Student stu : students) {
			names.add(stu.getUser().getGroupName());
		}
		for (Student stu : students) {
			emails.add(stu.getUser().getEmail());
		}
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		if (result.hasErrors())
			return new ModelAndView("student-form");
		for(String name : names) {
			if(name.equalsIgnoreCase(groupName)) {
				ModelAndView mav = new ModelAndView("student-form");
				mav.addObject("duplicate", true);
				return mav;
			}
		}
		for(String e : emails) {
			if(e.equals(email)) {
				ModelAndView mav = new ModelAndView("student-form");
				mav.addObject("email", true);
				return mav;
			}
		}
		ModelAndView mav = new ModelAndView("forward:/admin/students/list");
		student.getUser().setGroupName(groupName);
		student.getUser().setEmail(email);
		sService.createStudent(student);
		User stu = student.getUser();
		try {
			mService.sendSimpleMail(stu.getEmail(), "Account Created For "+stu.getGroupName(), "Username: "+stu.getGroupName()+ "\nPassword: password. \nPlease reset your password immediately after first login.");
			System.out.println("Success");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return mav;
	}

	@RequestMapping(value = "/students/list")
	@ResponseBody
	public ModelAndView studentList() {
		User user = userSessionService.findUserBySession();
		List<Booking> bookings = bService.findAllBookings();
		HashSet<String> studentIds = new HashSet<>();
		for (Booking b : bookings) {
			studentIds.add(b.getStudent().getId());
		}
		if (user != null) {
			if (!user.getRole().equals("ADMIN")) {
				ModelAndView mav = new ModelAndView("unauthorized-student");
				return mav;
			}
		}
		ModelAndView mav = new ModelAndView("students");
		List<Student> students = sService.findAllStudents();
		mav.addObject("students", students);
		mav.addObject("studentids", studentIds);
		return mav;
	}

	@RequestMapping(value = "/students/edit/{id}", method = RequestMethod.GET)
	public ModelAndView editProfile(@PathVariable String id) {
		User user = userSessionService.findUserBySession();
		Student stu = sService.findStudentById(id);
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		ModelAndView mav = new ModelAndView("student-profile-edit-for-admin");
		mav.addObject("student", stu);
		return mav;
	}

	@RequestMapping(value = "/students/edit", method = RequestMethod.POST)
	public ModelAndView saveProfile(@ModelAttribute @Valid Student student, BindingResult result) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		if (result.hasErrors())
			return new ModelAndView("student-profile-edit-for-admin");
		sService.editStudent(student);
		ModelAndView mav = new ModelAndView("students");
		List<Student> students = sService.findAllStudents();
		mav.addObject("students", students);
		return mav;
	}

	@RequestMapping(value = "/students/delete/{id}")
	public ModelAndView deleteStudent(@PathVariable("id") String id) {
		User user = userSessionService.findUserBySession();
		if (!user.getRole().equals("ADMIN")) {
			ModelAndView mav = new ModelAndView("unauthorized-student");
			return mav;
		}
		ModelAndView mav = new ModelAndView("forward:/admin/students/list");
		Student student = sService.findStudentById(id);
		sService.removeStudent(student);
		return mav;
	}
	
	@RequestMapping(value = "/dashboard")
	@ResponseBody
	public ModelAndView dashboard() {
		User user = userSessionService.findUserBySession();
		if (user != null) {
			if (!user.getRole().equals("ADMIN")) {
				ModelAndView mav = new ModelAndView("unauthorized-student");
				return mav;
			}
		}
		ModelAndView mav = new ModelAndView("dashboard");
		
		List<Room> rooms = rService.findAllRooms();
		List<Booking> bookings = br.findAllByDate(LocalDate.now());
		List<List<Object>> getBookingData = 
				List.of(
                List.of("SUCCESSFUL", bService.getBookingStatusCounts(bookings, BookingStatus.SUCCESSFUL)),
                List.of("REJECTED", bService.getBookingStatusCounts(bookings, BookingStatus.REJECTED)),
                List.of("CANCELLED", bService.getBookingStatusCounts(bookings, BookingStatus.CANCELLED)),
                List.of("WAITINGLIST", bService.getBookingStatusCounts(bookings, BookingStatus.WAITINGLIST))
        );
		
		boolean todayBooking = true;
		if (bService.getBookingStatusCounts(bookings, BookingStatus.SUCCESSFUL)==0 
				&&bService.getBookingStatusCounts(bookings, BookingStatus.REJECTED)==0
				&&bService.getBookingStatusCounts(bookings, BookingStatus.CANCELLED)==0
				&&bService.getBookingStatusCounts(bookings, BookingStatus.WAITINGLIST)==0){
			todayBooking = false;
		};

		LocalDate date = LocalDate.now();
		int week  = date.get(WeekFields.ISO.weekOfWeekBasedYear());
		int year = date.getYear();

//		LocalDate date = LocalDate.now();
//		int week  = date.get(WeekFields.ISO.weekOfWeekBasedYear());
//		int month = date.getMonth().getValue();
//		int year = date.getYear();
//		
//		Calendar calendar = Calendar.getInstance();
//		calendar.clear();
//		calendar.set(Calendar.WEEK_OF_YEAR, week);
//		calendar.set(Calendar.YEAR, year);
		
		LocalDate firstDayOfWeek = FirstDayOfCurrentWeek.value(LocalDate.now());
		System.out.println(firstDayOfWeek);
		
		List<Booking> weekBookings = bService.findBookingsInCurrentWeek(LocalDate.now());
		
		List<List<Object>> getWeekBookingData = 
				List.of(
                List.of("SUCCESSFUL", bService.getBookingStatusCounts(weekBookings, BookingStatus.SUCCESSFUL)),
                List.of("REJECTED", bService.getBookingStatusCounts(weekBookings, BookingStatus.REJECTED)),
                List.of("CANCELLED", bService.getBookingStatusCounts(weekBookings, BookingStatus.CANCELLED)),
                List.of("WAITINGLIST", bService.getBookingStatusCounts(weekBookings, BookingStatus.WAITINGLIST))
        );

		
		List<List<Object>> getOverallRoomForWeekData = 
				List.of(
                List.of("MONDAY", bService.getSuccessBookingsDurationForDate(weekBookings, firstDayOfWeek)),
                List.of("TUESDAY", bService.getSuccessBookingsDurationForDate(weekBookings, firstDayOfWeek.plusDays(1))),
                List.of("WEDNESDAY", bService.getSuccessBookingsDurationForDate(weekBookings, firstDayOfWeek.plusDays(2))),
                List.of("THURSDAY", bService.getSuccessBookingsDurationForDate(weekBookings, firstDayOfWeek.plusDays(3))),
                List.of("FRIDAY",
                	bService.getSuccessBookingsDurationForDate(weekBookings, firstDayOfWeek.plusDays(4)))
        );
		
		
		
		LocalDate firstDayOfMonth = firstDayOfWeek.withDayOfMonth(1);
		LocalDate lastDayOfMonth = firstDayOfWeek.withDayOfMonth(firstDayOfWeek.getMonth().length(firstDayOfWeek.isLeapYear()));
		
		List<Booking> monthBookings = br.findByDateBetween(firstDayOfMonth, lastDayOfMonth);
		
		List<List<Object>> getMonthBookingData = 
				List.of(
                List.of("SUCCESSFUL", bService.getBookingStatusCounts(monthBookings, BookingStatus.SUCCESSFUL)),
                List.of("REJECTED", bService.getBookingStatusCounts(monthBookings, BookingStatus.REJECTED)),
                List.of("CANCELLED", bService.getBookingStatusCounts(monthBookings, BookingStatus.CANCELLED)),
                List.of("WAITINGLIST", bService.getBookingStatusCounts(monthBookings, BookingStatus.WAITINGLIST))
        );
		
		List<Report> reports = ReService.findAllReports();
		
		List<List<Object>> getReportStatusData = 
				List.of(
                List.of("PROCESSING", ReService.getReportStatusCounts(reports, ReportStatus.PROCESSING)),
                List.of("REJECTED", ReService.getReportStatusCounts(reports, ReportStatus.REJECTED)),
                List.of("APPROVED", ReService.getReportStatusCounts(reports, ReportStatus.APPROVED))
        );
		
		List<List<Object>> getReportCatData = 
				List.of(
                List.of("CLEANLINESS", ReService.getReportCatCounts(reports, ReportCategory.CLEANLINESS)),
                List.of("VANDALISE", ReService.getReportCatCounts(reports, ReportCategory.VANDALISE)),
                List.of("HOGGING", ReService.getReportCatCounts(reports, ReportCategory.HOGGING)),
                List.of("MISUSE", ReService.getReportCatCounts(reports, ReportCategory.MISUSE))
        );
		
		Room beacon = rooms.stream()
				.filter(rm->rm.getRoomName().equalsIgnoreCase("Beacon")).findAny().get();
		Room frontier = rooms.stream()
				.filter(rm->rm.getRoomName().equalsIgnoreCase("Frontier")).findAny().get();
		Room jupiter = rooms.stream()
				.filter(rm->rm.getRoomName().equalsIgnoreCase("Jupiter")).findAny().get();
		Room mercury = rooms.stream()
				.filter(rm->rm.getRoomName().equalsIgnoreCase("Mercury")).findAny().get();	
		Room venus = rooms.stream()
				.filter(rm->rm.getRoomName().equalsIgnoreCase("Venus")).findAny().get();
		
//		List<String> roomNameList = rooms.stream()
//				.map(Room::getRoomName)
//				.collect(Collectors.toList());
//		rooms.forEach(e->ReService.getReportRoomCounts(reports, e));
//		
		List<List<Object>> getReportRoomData = 
				List.of(
                List.of("Beacon", ReService.getReportRoomCounts(reports, beacon)),
                List.of("Frontier", ReService.getReportRoomCounts(reports,frontier)),
                List.of("Jupiter", ReService.getReportRoomCounts(reports,jupiter)),
                List.of("Mercury", ReService.getReportRoomCounts(reports,mercury)),
                List.of("Venus", ReService.getReportRoomCounts(reports,venus))
        );
		
		List<List<Object>> getBookingFequencyData = 
				List.of(
                List.of("Beacon", bService.getBookingCountsForRoom(bService.findAllBookings(), beacon)),
                List.of("Frontier", bService.getBookingCountsForRoom(bService.findAllBookings(),frontier)),
                List.of("Jupiter", bService.getBookingCountsForRoom(bService.findAllBookings(),jupiter)),
                List.of("Mercury", bService.getBookingCountsForRoom(bService.findAllBookings(),mercury)),
                List.of("Venus", bService.getBookingCountsForRoom(bService.findAllBookings(),venus))
        );
		
		List<List<Object>> getTodayRoomUsageData = 
				List.of(
                List.of("Beacon", bService.getBookingHoursForRoom(bookings, beacon)),
                List.of("Frontier", bService.getBookingHoursForRoom(bookings,frontier)),
                List.of("Jupiter", bService.getBookingHoursForRoom(bookings,jupiter)),
                List.of("Mercury", bService.getBookingHoursForRoom(bookings,mercury)),
                List.of("Venus", bService.getBookingHoursForRoom(bookings,venus))
        );
		
		 long todayReport = ReService.findAllReports().stream()
				 .filter(r->r.getBooking().getDate()==date)
				 .count();
		
		long processingReports = ReService.findAllReports().stream()
				.filter(r->r.getReportStatus().equals(ReportStatus.PROCESSING))
				.count();	
		String monthPeriod = "Bookings For " + date.getMonth();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
		  String monday = firstDayOfWeek.format(formatter);
		  String friday = firstDayOfWeek.plusDays(4).format(formatter);
		String weekPeriod = "Bookings From " + monday +" to " + friday;
		
		String roomWeek = "Room Usage for " + monday +" to " + friday;
		String strTodayReport = "Reports filed Today: " + (int)todayReport;
		String strProcessReport = "Pending Reports to process: " + (int)processingReports;
		
		mav.addObject("getBookingData", getBookingData);
		System.out.println(getBookingData);
		mav.addObject("reports", reports);
		mav.addObject("strProcessReport", strProcessReport);
		mav.addObject("strTodayReport", strTodayReport);
		mav.addObject("getReportStatusData", getReportStatusData);
		mav.addObject("getWeekBookingData", getWeekBookingData);
		mav.addObject("getMonthBookingData", getMonthBookingData);
		mav.addObject("getOverallRoomForWeekData", getOverallRoomForWeekData);
		mav.addObject("getReportCatData", getReportCatData);
		mav.addObject("getReportRoomData", getReportRoomData);
		mav.addObject("todayBooking", todayBooking);
		mav.addObject("getBookingFequencyData", getBookingFequencyData);
		mav.addObject("getTodayRoomUsageData", getTodayRoomUsageData);
		mav.addObject("monthPeriod", monthPeriod);
		mav.addObject("weekPeriod", weekPeriod);
		mav.addObject("roomWeek", roomWeek);
		return mav;
	}
}