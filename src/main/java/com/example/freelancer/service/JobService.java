package com.example.freelancer.service;

import com.example.freelancer.dto.JobDTO;
import com.example.freelancer.dto.TransactionHistoryDTO;
import com.example.freelancer.entity.*;
import com.example.freelancer.repository.AccountRepository;
import com.example.freelancer.repository.FreelancerRepository;
import com.example.freelancer.repository.JobRepository;
import com.example.freelancer.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Configurable
public class JobService {
    @Autowired
    JobRepository jobRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    FreelancerRepository freelancerRepository;

    @Autowired
    SystemConfigRepository systemConfigRepository;

    @Autowired
    TransactionService transactionService;

    public Job getDetailJob(Integer id) {
        return jobRepository.findById(id).get();
    }

    public Job createJob(JobDTO jobDTO) {
        try {
            Job job = new Job();
            job.setSalary(jobDTO.getSalary());
            job.setSubject(jobDTO.getSubject());
            job.setDescription(jobDTO.getDescription());
            job.setType(2);
            if (jobDTO != null){
                job.setType(jobDTO.getType());
            }
            job.setStatus(1);
            job.setResult(jobDTO.getResult());
            job.setResponse_date(new Date());
            job.setAccountId(jobDTO.getAccountId());
            job.setAccount(accountRepository.findById(jobDTO.getAccountId()).get());
            job.setFreelancerId(jobDTO.getFreelancerId());
            job.setFreelancer(freelancerRepository.findById(jobDTO.getFreelancerId()).get());
            job.setCreated_at(new Date());
            job.setUpdated_at(new Date());
            job.setRate(jobDTO.getRate());
            job.setComment(jobDTO.getComment());
            jobRepository.save(job);
            return job;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public Job updateJob(JobDTO jobDTO) {
        Optional opt = jobRepository.findById(jobDTO.getId());
        if (opt.isPresent()) {
            Job job1 = (Job) opt.get();
            Freelancer freelancer = freelancerRepository.findById(jobDTO.getFreelancerId()).get();
            job1.setSalary(jobDTO.getSalary());
            job1.setSubject(jobDTO.getSubject());
            job1.setSubject(jobDTO.getSubject());
            job1.setDescription(jobDTO.getDescription());
            job1.setType(jobDTO.getType());
            job1.setStatus(jobDTO.getStatus());
            job1.setResult(jobDTO.getResult());
            job1.setResponse_date(jobDTO.getResponse_date());
            job1.setInvited_user_id(jobDTO.getInvited_user_id());
            job1.setAccountId(jobDTO.getAccountId());
            job1.setAccount(accountRepository.findById(jobDTO.getAccountId()).get());
            job1.setFreelancerId(jobDTO.getFreelancerId());
            job1.setFreelancer(freelancer);
            job1.setUpdated_at(new Date());
            job1.setRate(jobDTO.getRate());
            job1.setComment(jobDTO.getComment());

            if ((job1.getStatus() == 2 && job1.toJobDTO().getResult() == null) || (job1.getStatus() == 4)) {
                Optional<SystemConfig> optionalSystemConfig = systemConfigRepository.findTopByOrderById();
                SystemConfig systemConfig = new SystemConfig();
                TransactionHistory transactionHistory = new TransactionHistory();

                systemConfig.setAmount(0);
                if (optionalSystemConfig.isPresent()) {
                    systemConfig = optionalSystemConfig.get();
                }
                double systemConfigAmount = systemConfig.getAmount();
                Account freelancerAccount = freelancer.getAccount();
                if (job1.getStatus() == 2) {
                    Account account = accountRepository.getById(job1.getAccountId());
                    // Khi freelancer nhận job , thu tiền user tạo job
                    account.setAmount(account.getAmount() - job1.getSalary());
                    transactionHistory.setAmount(job1.getSalary());
                    transactionHistory.setType(2);
                    transactionHistory.setAccountId(job1.getAccountId());
                    transactionHistory.setAccount(job1.getAccount());
                } else {
                    double additionalFee = job1.getSalary() / 10;
                    // Freelancer done job , trả tiền cho freelancer trừ phí giao dijch
                    freelancerAccount.setAmount(freelancerAccount.getAmount() + (job1.getSalary() - additionalFee));
                    systemConfig.setAmount(systemConfigAmount - (job1.getSalary() - additionalFee));
                    transactionHistory.setAmount(-(job1.getSalary() - additionalFee));
                    transactionHistory.setType(1); // withdraw
                    transactionHistory.setAccountId(freelancerAccount.getId());
                    transactionHistory.setAccount(freelancerAccount);
                }
                transactionService.createTransactionHistory(transactionHistory);
                systemConfigRepository.save(systemConfig);
            }
            jobRepository.save(job1);

            if (job1.getStatus() == 4){
                Optional<Double> rate = jobRepository.getAvgRateByFreelancerId(freelancer.getId());

                if (rate.isPresent()){
                    freelancer.setRate(Math.round(rate.get() * 2) / 2);
                }
                freelancerRepository.save(freelancer);
            }
            return job1;
        }
        return null;
    }

    public int getTotalJobDone(int freelancerId) {
        return jobRepository.getTotalJobDone(freelancerId).size();
    }

    public double getTotalEarning(int freelancerId) {
        List<Job> lst = jobRepository.getTotalJobDone(freelancerId);
        Double sum = (double) 0;
        for (int i = 0; i < lst.size(); i++) {
            sum += lst.get(i).getSalary();
        }
        return sum;
    }

    public List<Job> getListJob() {
        return jobRepository.findAll();
    }

    public List<Job> getListJobByFreelancerId(int freelancerId) {
        return jobRepository.getListJobByFreelancerId(freelancerId);
    }

    public List<Job> getListJobByAccountId(Integer accountId) {
        return jobRepository.getListJobByAccountId(accountId);
    }

    public List<Job> getListJobByAccountIdAndFreelancerId(Integer accountId, Integer freelancerId) {
        return jobRepository.getListJobByAccountIdAndFreelancerId(accountId, freelancerId);
    }

    // admin
    public Page<Job> getListJobPagination(
            @Nullable Integer currentPage,
            @Nullable Integer pageSize
    ) {
        if (currentPage == null) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);

        Page<Job> result = jobRepository.findAll(pageable);
        return result;
    }

    public boolean deleteJob(int jobId) {
        Optional<Job> opt = jobRepository.findById(jobId);
        if (opt.isPresent()) {
            Job job = opt.get();
            job.setStatus(-1);
            jobRepository.save(job);
            return true;
        }
        return false;
    }

    public int statisticJobByStatus(int status) {
        return jobRepository.findAllByStatus(status).size();
    }

    public List<Object> getFinancial(Date startDate, Date endDate) {
        return jobRepository.getFinancial(startDate, endDate);
    }

    public List<Job> getListJobDoneByAccountId(Integer accountId) {
        return jobRepository.getListJobDoneByAccountId(accountId);
    }

    public List<Job> getListJobDoneByFreelancerId(Integer freelancerId) {
        return jobRepository.getListJobDoneByFreelancerId(freelancerId);
    }

    public long count() {
        return jobRepository.count();
    }

    public Job createJob2(Job jobDTO) {
        Job job = new Job();
        job.setSalary(jobDTO.getSalary());
        job.setSubject(jobDTO.getSubject());
        job.setDescription(jobDTO.getDescription());
        job.setType(jobDTO.getType());
        if (jobDTO != null){
            job.setType(jobDTO.getType());
        }
        job.setStatus(jobDTO.getStatus());
        job.setResult(jobDTO.getResult());
        job.setResponse_date(jobDTO.getResponse_date());
        job.setAccountId(jobDTO.getAccountId());
        job.setFreelancerId(jobDTO.getFreelancerId());
        job.setCreated_at(jobDTO.getCreated_at());
        job.setUpdated_at(jobDTO.getUpdated_at());
        job.setRate(jobDTO.getRate());
        job.setComment(jobDTO.getComment());
        jobRepository.save(job);

        return job;
    }
}
