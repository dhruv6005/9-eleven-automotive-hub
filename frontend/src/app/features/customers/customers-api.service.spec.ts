import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { CustomersApiService } from './customers-api.service';
import { API_BASE_URL } from '../../core/api-paths';

describe('CustomersApiService', () => {
  let api: CustomersApiService;
  let http: HttpTestingController;
  beforeEach(() => { TestBed.configureTestingModule({providers:[provideHttpClient(),provideHttpClientTesting()]});api=TestBed.inject(CustomersApiService);http=TestBed.inject(HttpTestingController); });
  afterEach(() => http.verify());
  it('uses the customer-owned vehicle endpoints and serializes registration payloads', async () => {
    const pending=api.saveMyVehicle(null,{make:'Honda',model:'City',variant:null,year:2022,color:null,sizeClass:'MEDIUM',regNumber:'MH01AB1234',vin:null});
    const request=http.expectOne(`${API_BASE_URL}/me/vehicles`);expect(request.request.method).toBe('POST');expect(request.request.body.regNumber).toBe('MH01AB1234');request.flush({id:'v1',customerId:'c1',make:'Honda',model:'City',variant:null,year:2022,color:null,sizeClass:'MEDIUM',regNumber:'MH01AB1234',vin:null});expect((await pending).id).toBe('v1');
  });
  it('applies admin customer search and status query parameters', async () => {
    const pending=api.customers('  abc  ','ACTIVE');const request=http.expectOne(r=>r.url===`${API_BASE_URL}/admin/customers`);expect(request.request.params.get('search')).toBe('abc');expect(request.request.params.get('status')).toBe('ACTIVE');expect(request.request.params.get('page')).toBe('0');request.flush({content:[],totalElements:0,totalPages:0,number:0,size:20});expect((await pending).content).toEqual([]);
  });
  it('loads vehicle size options from the shared public endpoint', async () => {
    const pending=api.sizes();const request=http.expectOne(`${API_BASE_URL}/public/vehicle-size-classes`);request.flush([{sizeClass:'SMALL',label:'Small'}]);expect((await pending)[0].sizeClass).toBe('SMALL');
  });
});
