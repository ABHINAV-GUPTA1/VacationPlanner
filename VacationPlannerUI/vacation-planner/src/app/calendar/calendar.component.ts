import { Component, OnInit } from '@angular/core';
import { HolidayService } from '../services/holiday.service';
import { DatePipe } from '@angular/common';
import { Country } from '../models/countries.model';
import { WeekData, HolidayDetailsList, HolidayDetails } from '../models/holiday.model';

@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css'],
  providers: [DatePipe],
})
export class CalendarComponent implements OnInit {
  currentDate: Date = new Date();
  calendars: Date[][] = [[], [], []];
  daysInMonth: Date[] = [];
  weekData: WeekData[] = [];
  holidayList: HolidayDetailsList[] = [];
  view: 'month' | 'quarter' = 'month';
  selectedQuarter: number = this.getCurrentQuarter();
  selectedYear: number = this.currentDate.getFullYear();
  quarters = [1, 2, 3, 4];
  countries: Country[] = [];
  loadingCountries: boolean = false;
  countryLoadError: string | null = null;
  selectedCountry: string = ''; // To store the selected country code
  isCountrySelected: boolean = false; // To track if a country is selected
  isYearSelected: boolean = false; // To track if a year is selected
  monthNames = [
    'January',
    'February',
    'March',
    'April',
    'May',
    'June',
    'July',
    'August',
    'September',
    'October',
    'November',
    'December',
  ];

   private holidayMap: { [date: string]: string } = {};
   private holidayColor: {[date: string]: string} = {};

  constructor(private holidayService: HolidayService, private datePipe: DatePipe) {}

  ngOnInit(): void {
    this.loadCountries(); // Load the countries
  }

  loadCountries(): void {
    this.loadingCountries = true;
    this.countryLoadError = null;

    this.holidayService.getCountries().subscribe({
      next: (countries) => {
        this.countries = countries;
        if (countries.length > 0) {
          this.selectedCountry = countries[0].countryCode; // Select the first country by default
        }
        this.loadingCountries = false;
      },
      error: (err) => {
        this.countryLoadError = 'Failed to load countries. Please try again later.';
        console.error('Error loading countries:', err);
        this.loadingCountries = false;
      }
    });
  }

  // When country is selected, fetch holidays and update the view
  onCountrySelect(countryCode: string): void {
    this.selectedCountry = countryCode;
    this.isCountrySelected = true; // Mark country as selected
    this.loadCalendarData(); // Load calendar data
  }

  // When year is selected, update the selected year
  onYearSelect(year: number): void {
    this.selectedYear = year;
    this.isYearSelected = true; // Mark year as selected
    this.loadCalendarData(); // Load calendar data
  }

  loadCalendarData(): void {
    if (this.selectedCountry && this.isYearSelected) {
      if (this.view === 'month') {
        this.generateMonthView();
        this.holidayService
          .getHolidaysByMonth(this.selectedYear, this.currentDate.getMonth() + 1, this.selectedCountry)
          .subscribe((holidayDetails) => {
            this.weekData = holidayDetails.weekInfoList;
            this.holidayList = holidayDetails.holidayDetailsList;
            this.updateHolidayMap();
            this.generateMonthView(); // regenerate to apply colors.
          });
      } else if (this.view === 'quarter') {
        this.generateQuarterView();
        this.holidayService
          .getHolidaysByQuarter(this.selectedYear, this.selectedQuarter, this.selectedCountry)
          .subscribe((holidayDetails) => {
            this.weekData = holidayDetails.weekInfoList;
            this.holidayList = holidayDetails.holidayDetailsList;
            this.updateHolidayMap();
            this.generateQuarterView();
          });
      }
    }
  }

  // Calendar views generation methods
  generateMonthView(): void {
    this.daysInMonth = [];
    const year = this.selectedYear;
    const month = this.currentDate.getMonth();
    this.currentDate = new Date(year, month, 1);
    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);

    const firstDayOfWeek = firstDayOfMonth.getDay();
    const paddingDays = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1;

    for (let i = 0; i < paddingDays; i++) {
      this.daysInMonth.push(new Date(year, month, 1 - paddingDays + i));
    }

    for (let i = 1; i <= lastDayOfMonth.getDate(); i++) {
      this.daysInMonth.push(new Date(year, month, i));
    }

    const lastDayOfWeek = lastDayOfMonth.getDay();
    const trailingDays = lastDayOfWeek === 0 ? 0 : 7 - lastDayOfWeek;

    for (let i = 1; i <= trailingDays; i++) {
      this.daysInMonth.push(new Date(year, month + 1, i));
    }
  }

  generateQuarterView(): void {
    // this.daysInMonth = [];
    // let startMonth = (this.selectedQuarter - 1) * 3; // 0, 3, 6, 9
    // for (let i = 0; i < 3; i++) {
    //   const year = this.selectedYear;
    //   const month = startMonth + i;
    //   const firstDayOfMonth = new Date(year, month, 1);
    //   const lastDayOfMonth = new Date(year, month + 1, 0);

    //   for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
    //     this.daysInMonth.push(new Date(year, month, day));
    //   }
    // }


    // this.daysInMonth = [];
    // let startMonth = (this.selectedQuarter - 1) * 3;
    // for (let i = 0; i < 3; i++) {
    //   const year = this.selectedYear;
    //   const month = startMonth + i;
    //   const firstDayOfMonth = new Date(year, month, 1);
    //   const lastDayOfMonth = new Date(year, month + 1, 0);
    //   const monthName = this.monthNames[month];
    //   const firstDayOfWeek = firstDayOfMonth.getDay();
    //   const paddingDays = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1;

    //   if (i > 0) {
    //     for (let j = 0; j < paddingDays; j++) {
    //       this.daysInMonth.push(new Date(year, month, -paddingDays + j));
    //     }
    //   }
    //   this.daysInMonth.push(new Date(year, month, -1)); // Month separator
    //   this.daysInMonth.push(new Date(year, month, 0));
    //   for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
    //     this.daysInMonth.push(new Date(year, month, day));
    //   }
    // }


    // this.calendars = [[], [], []];
    // let startMonth = (this.selectedQuarter - 1) * 3;
    // for (let i = 0; i < 3; i++) {
    //   const year = this.selectedYear;
    //   const month = startMonth + i;
    //   const firstDayOfMonth = new Date(year, month, 1);
    //   const lastDayOfMonth = new Date(year, month + 1, 0);
    //   const monthName = this.monthNames[month];
    //   const firstDayOfWeek = firstDayOfMonth.getDay();
    //   const paddingDays = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1;

    //   if (i > 0) {
    //     for (let j = 0; j < paddingDays; j++) {
    //       this.calendars[i].push(new Date(year, month, -paddingDays + j));
    //     }
    //   }
    //   // this.calendars[i].push(new Date(year, month, -1)); // Month separator
    //   // this.calendars[i].push(new Date(year, month, 0));
    //   for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
    //     this.calendars[i].push(new Date(year, month, day));
    //   }
    // }


    this.calendars = [[], [], []];
    let startMonth = (this.selectedQuarter - 1) * 3;

    for (let i = 0; i < 3; i++) {
      const year = this.selectedYear;
      const month = startMonth + i;
      const firstDayOfMonth = new Date(year, month, 1);
      const lastDayOfMonth = new Date(year, month + 1, 0);
      const firstDayOfWeek = firstDayOfMonth.getDay();
      const paddingDays = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1;

      // Add padding days before the 1st of the month
      for (let j = 0; j < paddingDays; j++) {
        const padDate = new Date(year, month, 1 - paddingDays + j);
        this.calendars[i].push(padDate);
      }

      // Add actual days of the month
      for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
        this.calendars[i].push(new Date(year, month, day));
      }

      // Optional: Pad at the end to fill up the final week (ensure 7xN grid)
      const totalDays = this.calendars[i].length;
      const remaining = totalDays % 7 === 0 ? 0 : 7 - (totalDays % 7);
      for (let j = 1; j <= remaining; j++) {
        this.calendars[i].push(new Date(year, month + 1, j));
      }
    }
  }

  goToPrevious(): void {
    if (this.view === 'month') {
      this.currentDate.setMonth(this.currentDate.getMonth() - 1);
      this.generateMonthView();
    } else if (this.view === 'quarter') {
      this.selectedQuarter--;
      if (this.selectedQuarter < 1) {
        this.selectedQuarter = 4;
        this.selectedYear--;
      }
      this.generateQuarterView();
    }
    this.loadCalendarData();
  }

  goToNext(): void {
    if (this.view === 'month') {
      this.currentDate.setMonth(this.currentDate.getMonth() + 1);
    } else if (this.view === 'quarter') {
      this.selectedQuarter++;
      if (this.selectedQuarter > 4) {
        this.selectedQuarter = 1;
        this.selectedYear++;
      }
    }
    this.loadCalendarData();
  }

  setView(newView: 'month' | 'quarter'): void {
    this.view = newView;
    this.isCountrySelected = true;
    this.isYearSelected = true;
    this.loadCalendarData();
  }

  selectQuarter(quarter: number): void {
    this.selectedQuarter = quarter;
    this.loadCalendarData();
  }

  getCurrentQuarter(): number {
    const month = this.currentDate.getMonth();
    return Math.floor(month / 3) + 1;
  }

  getWeekColor(date: Date, isIncReq: boolean): string {
    const formattedDate = this.datePipe.transform(date, 'yyyy-MM-dd');
    if (this.isWeekend(date, isIncReq)) {
      return 'lightgray';
    }
    // console.log(formattedDate+" "+date.getDay());
    if (this.holidayColor[formattedDate] || false) {
      const weekColor = (this.holidayColor[formattedDate] || 'white').toLowerCase();
      
      if (weekColor === 'light_green') { //keep underscore to handle the data
            // console.log(weekColor+" "+date);
        return 'lightgreen';
      } else if (weekColor === 'white') {
        return 'white';
      } else if (weekColor === 'dark_green') {
        return 'darkgreen';
      }
    }

    return 'white';
  }

  isWeekend(date: Date, isIncReq: boolean): boolean {
    let day = date.getDay();
    
    return (day === 0 || day === 6); // 0 for Sunday, 6 for Saturday
  }

  getHolidayName(date: Date): string | null {
    const formattedDate = this.datePipe.transform(date, 'yyyy-MM-dd');
    return this.holidayMap[formattedDate] || null;
  }

  private updateHolidayMap(): void {
    this.holidayMap = {};
    if (this.holidayList) {
      this.holidayList.forEach((holiday) => {
        const startDate = new Date(holiday.date);
        const formattedDate = this.datePipe.transform(startDate, 'yyyy-MM-dd');
        this.holidayMap[formattedDate] = holiday.name;
      });
    }
    this.holidayColor = {};
    if (this.weekData) {
      for (const week of this.weekData) {
        const startDate = new Date(week.startOfWeek);
        const endDate = new Date(week.endOfWeek);
        for (let date = startDate; date <= endDate; date.setDate(date.getDate() + 1)) {
          if (week.color) {
            const formattedDate = this.datePipe.transform(date, 'yyyy-MM-dd');
            this.holidayColor[formattedDate] = week.color;
          }
        }
      }
    }
  }

}