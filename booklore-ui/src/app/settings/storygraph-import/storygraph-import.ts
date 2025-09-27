import {Component, inject} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ButtonModule } from 'primeng/button';
import {StorygraphService} from "./storygraph.service";
import {ToastModule} from "primeng/toast";
import {MessageService} from "primeng/api";

@Component({
  selector: 'app-storygraph-import',
  standalone: true,
  imports: [CommonModule, ButtonModule, ToastModule],
  providers: [MessageService],
  templateUrl: './storygraph-import.html',
  styleUrl: './storygraph-import.scss'
})
export class StorygraphImport {
  selectedFile: File | null = null;
  private storygraphService = inject(StorygraphService);
  private messageService = inject(MessageService);

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    } else {
      this.selectedFile = null;
    }
  }

  onUpload(): void {
    if (this.selectedFile) {
      this.storygraphService.importCsv(this.selectedFile).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Import completed successfully' });
        },
        error: (error) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Import failed' });
          console.error('Import error:', error);
        }
      });
    }
  }
}