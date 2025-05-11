
export interface WeekData {
  startOfWeek: string;
  endOfWeek: string;
  color: string;
}
export interface HolidayDetailsList {
  name: string;
  date: string;
}
export interface HolidayDetails {
  weekInfoList: WeekData[];
  holidayDetailsList: HolidayDetailsList[];
}