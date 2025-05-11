import { Component, OnInit } from '@angular/core';
import { HolidayService } from '../services/holiday.service';
import { DatePipe } from '@angular/common';
// import { FormsModule } from '@angular/forms'; // Import FormsModule
import { Country } from '../models/countries.model';
import { WeekData } from '../models/holiday.model';


@Component({
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css'],
  providers: [DatePipe], // Add DatePipe to the providers array
  // standalone: true, // Add standalone: true
})
export class CalendarComponent implements OnInit {
  currentDate: Date = new Date();
  daysInMonth: Date[] = [];
  weekData: WeekData[] = [];
  view: 'month' | 'quarter' = 'month';
  selectedQuarter: number = this.getCurrentQuarter();
  selectedYear: number = this.currentDate.getFullYear();
  quarters = [1, 2, 3, 4];
  countries: Country[] = [];
  loadingCountries: boolean = false;
  countryLoadError: string | null = null;
  selectedCountry: string = ''; // To store the selected country code

  constructor(private holidayService: HolidayService, private datePipe: DatePipe) {} // Inject DatePipe

  ngOnInit(): void {
    this.loadCountries(); // Load the countries
  }

  loadCalendarData(): void {
    console.log('loadCalendarData() called with selectedCountry:', this.selectedCountry);
    if (this.selectedCountry) {
      // only load if country is selected
      if (this.view === 'month') {
        this.generateMonthView();
        this.holidayService
          .getHolidaysByMonth(
            this.selectedYear,
            this.currentDate.getMonth() + 1,
            this.selectedCountry
          )
          .subscribe((weekData) => {
            this.weekData = weekData;
            console.log('Month data received:', this.weekData);
            this.generateMonthView(); //regenerate to apply colors.
          });
      } else if (this.view === 'quarter') {
        this.generateQuarterView();
        this.holidayService
          .getHolidaysByQuarter(
            this.selectedYear,
            this.selectedQuarter,
            this.selectedCountry
          )
          .subscribe((weekData) => {
            this.weekData = weekData;
            console.log('Quarter data received:', this.weekData);
            this.generateQuarterView();
          });
      }
    }
  }

  // loadCountries(): void {
  //   this.holidayService.getCountries().subscribe((countries) => {
  //     this.countries = countries;
  //     console.log('Countries received:', this.countries);
  //     if (this.countries.length > 0) {
  //       this.selectedCountry = this.countries[0].countryCode; // Select the first country by default.        
  //     }
  //     this.loadCalendarData(); //call loadCalendarData here
  //   });
  // }

  loadCountries(): void {
    this.loadingCountries = true;
    this.countryLoadError = null;

    this.holidayService.getCountries().subscribe({
      next: (countries) => {
        this.countries = countries;
        if (countries.length > 0) {
          this.selectedCountry = countries[0].countryCode;
          this.loadCalendarData();
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

  generateMonthView(): void {
    this.daysInMonth = [];
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();
    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);

    // Add padding for the first week if it doesn't start on Monday
    const firstDayOfWeek = firstDayOfMonth.getDay(); // 0 for Sunday, 1 for Monday, etc.
    const paddingDays = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1; // Adjust so Monday is the start

    for (let i = 0; i < paddingDays; i++) {
      this.daysInMonth.push(new Date(year, month, 1 - paddingDays + i));
    }

    for (let i = 1; i <= lastDayOfMonth.getDate(); i++) {
      this.daysInMonth.push(new Date(year, month, i));
    }

    // Add padding for the last week if it doesn't end on Sunday
    const lastDayOfWeek = lastDayOfMonth.getDay(); // 0 for Sunday, 1 for Monday, etc.
    const trailingDays = lastDayOfWeek === 0 ? 0 : 7 - lastDayOfWeek;

    for (let i = 1; i <= trailingDays; i++) {
      this.daysInMonth.push(new Date(year, month + 1, i));
    }
  }

  generateQuarterView(): void {
    this.daysInMonth = [];
    let startMonth = (this.selectedQuarter - 1) * 3; // 0, 3, 6, 9
    for (let i = 0; i < 3; i++) {
      const year = this.selectedYear;
      const month = startMonth + i;
      const firstDayOfMonth = new Date(year, month, 1);
      const lastDayOfMonth = new Date(year, month + 1, 0);

      // Add days of the month to the daysInMonth array
      for (let day = 1; day <= lastDayOfMonth.getDate(); day++) {
        this.daysInMonth.push(new Date(year, month, day));
      }
    }
    // Basic quarter view, you might want to format this more visually
  }

  isWeekend(date: Date): boolean {
    const day = date.getDay();
    return day === 0 || day === 6; // 0 for Sunday, 6 for Saturday
  }

  getWeekColor(date: Date): string {
    const formattedDate = this.datePipe.transform(date, 'yyyy-MM-dd');
    if (this.isWeekend(date)) {
      return 'lightgray';
    }
    if (this.weekData) {
      for (const week of this.weekData) {
        const startDate = new Date(week.startOfWeek);
        const endDate = new Date(week.endOfWeek);
        if (date >= startDate && date <= endDate) {
          const weekColor = week.color.toLowerCase();
          if (weekColor === 'light_green') { //keep underscore to handle the data
            return 'lightgreen';
          } else if (weekColor === 'white') {
            return 'white';
          }
          return weekColor; // Or return the original if it's a custom color
        }
      }
    }
    return 'white';
  }

  goToPrevious(): void {
    if (this.view === 'month') {
      // Decrease month
      this.currentDate.setMonth(this.currentDate.getMonth() - 1);
      // Reset the daysInMonth after month change
      this.generateMonthView();
    } else if (this.view === 'quarter') {
      // Decrease quarter
      this.selectedQuarter--;
      if (this.selectedQuarter < 1) {
        this.selectedQuarter = 4;
        this.selectedYear--;
      }
      this.generateQuarterView(); // Regenerate quarter view
    }
    this.loadCalendarData(); // Fetch the data for the updated month or quarter
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
}