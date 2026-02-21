import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'humanDate',
  standalone: true,
})
export class HumanDatePipe implements PipeTransform {
  transform(value: string | Date | null | undefined): string {
    if (!value) {
      return 'N/A';
    }

    const resolvedDate = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(resolvedDate.getTime())) {
      return 'N/A';
    }

    return new Intl.DateTimeFormat(undefined, {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(resolvedDate);
  }
}
