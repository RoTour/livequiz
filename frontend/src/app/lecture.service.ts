import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' }) // Singleton, available application-wide
export class LectureService {
  private http = inject(HttpClient);
  private endpoint = '/api/lectures';

  create(dto: CreateLectureDto): Observable<{ id: string }> {
    return this.http.post<{ id: string }>(this.endpoint, dto);
  }
}

type CreateLectureDto = {
  title: string;
};
