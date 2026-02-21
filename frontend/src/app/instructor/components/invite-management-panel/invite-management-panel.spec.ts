import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { InviteManagementPanel } from './invite-management-panel';

describe('InviteManagementPanel', () => {
  let fixture: ComponentFixture<InviteManagementPanel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InviteManagementPanel],
    }).compileComponents();

    fixture = TestBed.createComponent(InviteManagementPanel);
    fixture.componentRef.setInput('selectedLectureId', 'lecture-1');
    fixture.componentRef.setInput('lastCreatedInvite', null);
    fixture.componentRef.setInput('invites', []);
    fixture.detectChanges();
  });

  it('emits create and refresh events from top-level actions', () => {
    const createInvite = vi.fn();
    const refreshInvites = vi.fn();
    fixture.componentInstance.createInvite.subscribe(createInvite);
    fixture.componentInstance.refreshInvites.subscribe(refreshInvites);

    findButton(fixture, 'Generate invite').click();
    findButton(fixture, 'Refresh invites').click();

    expect(createInvite).toHaveBeenCalledTimes(1);
    expect(refreshInvites).toHaveBeenCalledTimes(1);
  });

  it('renders invite states and emits revoke only for active invite', () => {
    const revokeInvite = vi.fn();
    fixture.componentInstance.revokeInvite.subscribe(revokeInvite);

    fixture.componentRef.setInput('lastCreatedInvite', {
      inviteId: 'inv-new',
      lectureId: 'lecture-1',
      joinCode: 'NEW123',
      joinUrl: 'https://example.com/student/join/t-new',
      expiresAt: '2026-02-21T12:00:00Z',
      active: true,
    });
    fixture.componentRef.setInput('invites', [
      {
        inviteId: 'inv-active',
        lectureId: 'lecture-1',
        joinCode: 'ACT123',
        createdAt: '2026-02-21T08:00:00Z',
        expiresAt: '2026-02-21T12:00:00Z',
        revokedAt: null,
        active: true,
      },
      {
        inviteId: 'inv-revoked',
        lectureId: 'lecture-1',
        joinCode: 'REV123',
        createdAt: '2026-02-21T08:00:00Z',
        expiresAt: '2026-02-21T12:00:00Z',
        revokedAt: '2026-02-21T09:00:00Z',
        active: false,
      },
      {
        inviteId: 'inv-expired',
        lectureId: 'lecture-1',
        joinCode: 'EXP123',
        createdAt: '2026-02-21T08:00:00Z',
        expiresAt: '2026-02-21T09:00:00Z',
        revokedAt: null,
        active: false,
      },
    ]);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('NEW123');
    expect(compiled.textContent).toContain('https://example.com/student/join/t-new');
    expect(compiled.textContent).toContain('active');
    expect(compiled.textContent).toContain('revoked');
    expect(compiled.textContent).toContain('expired');

    const revokeButtons = Array.from(compiled.querySelectorAll('.lq-question-item button')) as HTMLButtonElement[];
    expect(revokeButtons).toHaveLength(3);
    expect(revokeButtons[0].disabled).toBe(false);
    expect(revokeButtons[1].disabled).toBe(true);
    expect(revokeButtons[2].disabled).toBe(true);

    revokeButtons[0].click();
    expect(revokeInvite).toHaveBeenCalledWith('inv-active');
  });
});

function findButton(fixture: ComponentFixture<InviteManagementPanel>, label: string): HTMLButtonElement {
  const buttons = Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
  const button = buttons.find((candidate) => candidate.textContent?.trim() === label);
  if (!button) {
    throw new Error(`Button not found: ${label}`);
  }
  return button;
}
