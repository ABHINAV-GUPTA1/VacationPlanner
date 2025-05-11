import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs'; // Import throwError
import { catchError } from 'rxjs/operators';  // Import catchError
import { WeekData } from '../models/holiday.model';
import { Country } from '../models/countries.model';

@Injectable({
  providedIn: 'root',
})
export class HolidayService {
  private apiUrl = 'http://localhost:8080/api/v1/holidays';
  private headers = new HttpHeaders({ 'Content-Type': 'application/json' });
  // If authentication is needed, add the Authorization header
  // private headers = new HttpHeaders({ 'Content-Type': 'application/json', 'Authorization': 'Bearer <your_token>' });

  constructor(private http: HttpClient) {}

  private handleError(error: any) { // Added handleError
    console.error('HolidayService Error:', error); // Log the error
    return throwError(error);
  }

  getHolidays(): Observable<WeekData[]> {
    return this.http.get<WeekData[]>(this.apiUrl, { headers: this.headers }).pipe(
      catchError(this.handleError)
    );
  }

  getHolidaysByMonth(year: number, month: number, countryCode: string): Observable<WeekData[]> {
    const url = `${this.apiUrl}/weeks?countryCode=${countryCode}&year=${year}&month=${month}`;
    return this.http.get<WeekData[]>(url, { headers: this.headers }).pipe(
      catchError(this.handleError)
    );
  }

  getHolidaysByQuarter(year: number, quarter: number, countryCode: string): Observable<WeekData[]> {
    const url = `${this.apiUrl}/weeks?countryCode=${countryCode}&year=${year}&quarter=${quarter}`;
    return this.http.get<WeekData[]>(url, { headers: this.headers }).pipe(
      catchError(this.handleError)
    );
  }

  getCountries(): Observable<Country[]> {
    const url = `${this.apiUrl}/countries`;
    return this.http.get<Country[]>(url, { headers: this.headers }).pipe(
      catchError(this.handleError)
    );
  }
}