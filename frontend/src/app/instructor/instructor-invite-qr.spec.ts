import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import QRCode from 'qrcode';
import { vi } from 'vitest';
import { InstructorInviteQr } from './instructor-invite-qr';

describe('InstructorInviteQr', () => {
  let fixture: ComponentFixture<InstructorInviteQr>;

  afterEach(() => {
    window.location.hash = '';
    vi.restoreAllMocks();
  });

  it('renders QR code from hash payload and clears the hash', async () => {
    window.location.hash = encodeURIComponent(
      JSON.stringify({
        joinCode: 'ABCD12',
        joinUrl: 'https://example.com/student/join/t-1',
      }),
    );

    const qrCode = QRCode as unknown as { toDataURL: (...args: unknown[]) => Promise<string> };
    vi.spyOn(qrCode, 'toDataURL').mockResolvedValue('data:image/png;base64,preview');

    await TestBed.configureTestingModule({
      imports: [InstructorInviteQr],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ inviteId: 'inv-1' }),
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InstructorInviteQr);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const qrImage = fixture.nativeElement.querySelector('img') as HTMLImageElement | null;
    expect(qrImage).not.toBeNull();
    expect(qrImage?.getAttribute('src')).toBe('data:image/png;base64,preview');
    expect(window.location.hash).toBe('');
  });

  it('shows error when invite preview payload is missing', async () => {
    await TestBed.configureTestingModule({
      imports: [InstructorInviteQr],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ inviteId: 'missing' }),
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InstructorInviteQr);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Invite preview expired. Generate a new invite from the lecture page.');
  });
});
