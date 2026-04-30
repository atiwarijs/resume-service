import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have empty email and password initially', () => {
    expect(component.email).toBe('');
    expect(component.password).toBe('');
  });

  it('should not login with empty credentials', () => {
    spyOn(window, 'alert');
    component.onLogin();
    expect(window.alert).toHaveBeenCalledWith('Please enter both email and password');
  });

  it('should set isLoading to true during login', () => {
    component.email = 'test@example.com';
    component.password = 'password123';
    component.onLogin();
    expect(component.isLoading).toBe(true);
  });
});
