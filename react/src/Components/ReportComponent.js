import { MDBFooter } from 'mdb-react-ui-kit';
import { Component } from 'react';
import { Button, ButtonGroup, Container } from 'reactstrap';
import "../App.css";
import { Navbar, Nav, NavDropdown } from 'react-bootstrap';
import './mycss.css';
// import { Link } from 'react-router-dom';



class Report extends Component {

    constructor(props) {
        super(props);
        this.state = {
            reports: [],
            dateFilter: '',
            isLoaded: false,
            isOpen: false,
            imageOpen: '',
            statusFilter: 'PROCESSING',
            categoryFilter: ''
        };

        this.onChange = {
            dateFilter: this.handleChange.bind(this, 'dateFilter'),
            statusFilter: this.handleChange.bind(this, 'statusFilter'),
            categoryFilter: this.handleChange.bind(this, 'categoryFilter')
            
        }
    };

    async componentDidMount() {
        fetch('/admin/reports')
            .then(response => response.json())
            .then(data => this.setState({ reports: data, isLoaded: true }));
    }

    async updateReport(id, status) {
        if (status === "APPROVE") {
            if (window.confirm("Please confirm approval.")) {
                await fetch(`/admin/reports/approval/${id}`, {
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    }
                })
                    .then(
                        window.location.reload(false)
                    )
            };
        }
        else {
            if (window.confirm("Please confirm rejection.")) {
                await fetch(`/admin/reports/reject/${id}`, {
                    method: 'POST',
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    }
                })
                    .then(
                        window.location.reload(false)
                    )
            };
        }
    };



    handleShowDialog = value => {
        this.setState({ isOpen: !this.state.isOpen });
        this.setState({ imageOpen: value })
        console.log(this.state.imageOpen)
        console.log("clicked");
    };


    handleChange(name, event) {
        this.setState({ [name]: event.target.value });
    };

    render() {
        const { reports, isLoaded } = this.state;

        if (!isLoaded) {
            return <p>Loading...</p>;
        }

        // const ReportListProcessed = reports.forEach(x => x.imgPath = "/img/"+x.imgPath);
        const ReportList = reports
            .filter(report =>
                report.booking.date.includes(this.state.dateFilter)&&report.reportStatus.includes(this.state.statusFilter)&&report.category.includes(this.state.categoryFilter))
            .map(searchedReports => {
                return (
                    <tr>
                        <td>{searchedReports.id}</td>
                        <td>{searchedReports.category}</td>
                        <td> <img className="preview"
                            src={searchedReports.imgPath}
                            onClick={() => this.handleShowDialog(searchedReports.imgPath)}
                            alt="report_image" />
                            {this.state.isOpen && (
                                <dialog
                                    className="dialog"
                                    style={{ position: "fixed" }}
                                    open
                                    onClick={() => this.handleShowDialog(searchedReports.imgPath)}
                                >
                                    <img
                                        className="dialogImage"
                                        src={this.state.imageOpen}
                                        onClick={() => this.handleShowDialog(searchedReports.imgPath)}
                                        alt="report_image_open" />
                                </dialog>
                            )}</td>
                        <td>{searchedReports.details}</td>
                        <td>{searchedReports.booking.date} / {searchedReports.booking.time}</td>
                        <td>{searchedReports.booking.duration} minutes</td>
                        <td>{searchedReports.reportStatus}</td>
                        {/* <td>{searchedReports.room.roomName}</td> */}
                        <td>
                            <ButtonGroup>
                                <Button size="sm" color='primary' onClick={() => this.updateReport(searchedReports.id, "APPROVE")} style={{ display: (searchedReports.reportStatus === "PROCESSING")? 'block' : 'none'}}>Approve<span className="fa fa-thumbs-up"></span></Button>
                                <Button size="sm" color='danger' onClick={() => this.updateReport(searchedReports.id, "REJECT")} style={{ display: (searchedReports.reportStatus === "PROCESSING")? 'block' : 'none' }}>Reject<span className="fa fa-times"></span></Button>
                            </ButtonGroup></td>
                    </tr>
                );
            });

        return (
            <div className=''>
                <Navbar bg="light" style={{ paddingLeft: '15px', paddingRight: '15px' }}>
                    <Navbar.Brand href="http://localhost:8080/admin/index" >
                        <img src="/images/logo.png" width="131.375px" height="60px" />
                    </Navbar.Brand>
                    <Nav>
                        <Nav.Link href="http://localhost:8080/admin/index">Home</Nav.Link>
                    </Nav>
                    <Nav>
                        <Nav.Link href="http://localhost:8080/admin/dashboard">Dashboard</Nav.Link>
                    </Nav>
                    <Nav>
                        <NavDropdown title="Manage Rooms">
                            <NavDropdown.Item href="http://localhost:8080/admin/rooms/create">Create Room</NavDropdown.Item>
                            <NavDropdown.Item href="http://localhost:8080/admin/rooms/list">Room List</NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                    <Nav>
                        <NavDropdown title="Manage Students">
                            <NavDropdown.Item href="http://localhost:8080/admin/students/create">Create Student</NavDropdown.Item>
                            <NavDropdown.Item href="http://localhost:8080/admin/students/list">Student List</NavDropdown.Item>
                        </NavDropdown>
                    </Nav>
                    <Nav>
                        <Nav.Link href="/admin/bookings">Manage Bookings</Nav.Link>
                        <Nav.Link href="/admin/reports">Manage Reports</Nav.Link>
                    </Nav>
                    <Nav.Link href="http://localhost:8080/logout" className="btn btnorange" >Logout</Nav.Link>


                </Navbar>
                <Container className='mt-5'>
                    <div className="float-end">
                        {/* <label for="studentname">Student search:&nbsp;&nbsp;</label>
                    <input type="text" onChange={this.onChange.studentFilter} id="studentname" /> */}
                        <label for="date">Date:&nbsp;&nbsp;</label>
                        <input type="date" onChange={this.onChange.dateFilter} id="date" min="2022-01-01" max="2023-12-31" style={{marginRight: '25px'}}></input>
                        <label for="status">Status:&nbsp;&nbsp;</label>
                        <select id="status" onChange={this.onChange.statusFilter} style={{marginRight: '25px'}}>
                            <option value="PROCESSING">Processing</option>
                            <option value="APPROVED">Approved</option>
                            <option value="REJECTED">Rejected</option>
                        </select>
                        <label for="category">Category:&nbsp;&nbsp;</label>
                        <select id="category" onChange={this.onChange.categoryFilter} >
                            <option disabled selected value></option>
                            <option value="CLEANLINESS">Cleanliness</option>
                            <option value="VANDALISE">Vandalise</option>
                            <option value="HOGGING">Hogging</option>
                            <option value="MISUSE">Misuse</option>
                        </select>
                    </div>
                    <div>
                        <h2>Report List</h2>
                    </div>
                    <table className='table table-hover text-center mt-3' style={{marginBottom: '100px'}}>
                        <thead className='table-light'>
                            <tr>
                                <th>Report ID</th>
                                <th>Category</th>
                                <th>Image</th>
                                <th>Details</th>
                                <th>Booking Date & Time</th>
                                <th>Duration</th>
                                <th>Status</th>
                                <th>Options</th>
                            </tr>
                        </thead>
                        <tbody>
                            {ReportList}
                        </tbody>
                    </table>
                </Container>
                <MDBFooter bgColor='light' className='text-center text-lg-start text-muted'>
                    <div className='text-center p-4 myfooter' style={{ backgroundColor: '#003062', color: 'white', position: 'fixed' }}>
                        VMS Copyright ?? 2022
                    </div>
                </MDBFooter>
            </div>
        );
    }
}
export default Report;