import pandas as pd
import matplotlib.pyplot as plt
import sys

from click._compat import raw_input

# enter the full path of the file name
print("\nPlease enter the full path of the input file name(CSV only) (Example: D:\Python Project\cz_authtxn.csv)")
inFile = raw_input("Dataset Full Path: ")

print("\nPlease enter where the preprocess files should outputed (Example: D:\Python Project\\)")
outFilePath = raw_input("Output Path: ")

df = pd.read_csv(inFile, skipinitialspace=True)
# df = pd.read_csv('D:/Python Project/Credit Card Fraud Detection/cardzone dataset/cz_authtxn.csv', skipinitialspace=True)

# print number of records in the dataset
print("\nInformation about the dataset")
print("=======================================")
print("Number of records:" , len(df))
print("Number of features:" , len(df.columns))

# check is there any null value in cells at columns
print("Number of features that has empty cells" , len(df.columns[df.isna().any()]))

print("Names of the columns that has empty cells: ")
print(df.columns[df.isna().any()], "\n")

# Checks how many fraud in this dataset
print(df['AUTHTXN_FRAUD_CHECK'].value_counts())
print('Fraud is {}% of our data.'.format(df['AUTHTXN_FRAUD_CHECK'].value_counts()['D'] / float(df['AUTHTXN_FRAUD_CHECK'].value_counts()['F'])*100))

# Check Class variables that has 0 value for Genuine transactions and 1 for Fraud
print("\nShowing pie chart...\n")
fig, ax = plt.subplots(1, 1)
ax.pie(df.AUTHTXN_FRAUD_CHECK.value_counts(),autopct='%1.1f%%', labels=['Legitimate','Fraud'], colors=['yellowgreen','r'])
plt.axis('equal')
plt.ylabel('')
plt.show()

print("==================== Start Pre-processing ====================")
'''
Data Pre-processing - Deal with missing values
There are several strategies to deal with missing data and there is no exact right answer
- A value from another randomly selected record.
- A mean, median or mode value for the column. (reduces variance in the dataset)
    - Categorical NaNs for mode
    - Numerical NaNs for mean
    - If there are outliers in Numerical, try median (less sensitive to them)
- Drop those record / column
- A value estimated by another predictive model.
- A distinct constant value, such as 0 or -9999

However, missing values does not necessarily means to missing information. 
For example, if someone does not own a car, then of course it has no color resulting in missing value
If replace the missing value with some other value might leads to wrong result

For our case, several pre-processing strategies is follows:
- Drop columns which has over 70% NaN(Blank) values
- Drop columns according to Prototype Design Document (based on human intuition)
- Drop columns which has only 1 unique value
- Replace 0 to the missing values in columns / df.dropna
- Clean Date and Time
- Move AUTHTXN_FRAUD_CHECK to last columns for easier to see
'''
print("============================================")
print("Pre-processing Stage 1: Drop Columns")
print("============================================")
# First, print out the exact 30% records number that i want to keep the column
print("Drop Columns that has >70% NaN(Blank) values")
print("--------------------------------------------")
print("Number of records that i want to keep in columns : ", len(df) * 0.3)

print("Start dropping any columns that has 70% NaN...")
df.dropna(thresh=0.3*len(df), axis=1, inplace=True)
print("Drop Done")
# print list of columns after initial drop
print("The number of columns after initial dropping is ", len(df.columns))
print("The names of columns: ")
print(df.columns)

# Recheck still got any left out blank cell in the columns that does not meet threshold
print("\nThe number of blank records in columns after initial dropping is ", len(df.columns[df.isna().any()]))

print("Names of the columns that still has empty cells: ")
print(df.columns[df.isna().any()], "\n")

# Drop unnecessary columns according to prototype design document
print("Drop unnecessary columns based on columns' attributes")
print("-----------------------------------------------------")
print("Droping columns according to Prototype Design Document (based on human intuition)...")
drop_columns = ['AUTHTXN_NO', 'AUTHTXN_CARDHOLDER_NAME', 'AUTHTXN_SYSTEM_ID', 'AUTHTXN_APPROVED_AMT', 'AUTHTXN_STAN',
                'AUTHTXN_PREV_STAN', 'AUTHTXN_TRANS_DATETIME', 'AUTHTXN_REQUEST_DATE', 'AUTHTXN_REQUEST_TIME', 'AUTHTXN_RESPONSE_DATE',
                'AUTHTXN_RESPONSE_TIME', 'AUTHTXN_SETTLED_DATE', 'AUTHTXN_LAST_UPDATE_DATE', 'AUTHTXN_LAST_UPDATE_TIME', 'AUTHTXN_CARD_EXPIRY_DATE',
                'AUTHTXN_POS_COND_CODE', 'AUTHTXN_RETRIEVAL_REFNO', 'AUTHTXN_OLD_RETRIEVAL_REFNO', 'AUTHTXN_APPROVAL_CODE', 'AUTHTXN_RESPONSE_CODE',
                'AUTHTXN_MERCHANT_NAME', 'AUTHTXN_SETTLED_IND', 'AUTHTXN_AUTO_EXPIRY_DATE', 'AUTHTXN_SUBSIDY_REBATE_AMT', 'AUTHTXN_MERC_MDR_AMT',
                'AUTHTXN_MERC_COMM_AMT', 'AUTHTXN_PROCESSEDBY', 'AUTHTXN_TYPE', 'AUTHTXN_INTERCHG_IND', 'AUTHTXN_GEOGRAPHY_IND',
                'AUTHTXN_BONUS','AUTHTXN_FEE', 'AUTHTXN_ACQ_CHARGE_AT_IND', 'AUTHTXN_POST_IND', 'AUTHTXN_COMPONENT_ID',
                'AUTHTXN_MTI', 'AUTHTXN_PROC_CD', 'AUTHTXN_BONUS_POINT', 'AUTHTXN_TERMBONUS_POINT', 'VERSION',
                'AUTHTXN_SUBSIDY_REBATE_QTY', 'AUTHTXN_STMT_INC_BONUS', 'AUTHTXN_STMT_INC_FEE', 'AUTHTXN_STMT_INC_COST', 'AUTHTXN_STMT_INC_COMM',
                'AUTHTXN_EBONUS', 'AUTHTXN_EFEE', 'AUTHTXN_SERVICE_CODE', 'AUTHTXN_ALT_RESPONSE_CODE', 'AUTHTXN_GST_AMT',
                'AUTHTXN_MERC_GST_AMT', 'AUTHTXN_VTXNTYPGRP_ID', 'AUTHTXN_EDC_SETTLED_IND', 'AUTHTXN_VS_TRXN_ID', 'AUTHTXN_MATCH_PREVTXN_IND',
                'AUTHTXN_ACQ_INST_ID', 'AUTHTXN_EDC_SETTLED_DATE', 'AUTHTXN_INTERBRANCH_IND', 'AUTHTXN_PIN_BASED', 'AUTHTXN_EXP_IND',
                'AUTHTXN_FOREX_MARKUP_AMT', 'AUTHTXN_EXCESS_AMT']
for col in drop_columns:
    df.drop(col, axis=1, inplace=True)
print("Drop Done")
print("The number of columns after second dropping is ", len(df.columns))
print(df.columns)
print("The number of blank records in columns after second dropping is ", len(df.columns[df.isna().any()]))
print(df.columns[df.isna().any()], "\n")

# Drop columns if only have one unique value
print("Drop columns if only have one unique value")
print("-------------------------------------------")
print("First, check unique values count in columns")
print(df.nunique())
print("\nStart dropping columns that only have 1 unique values...")
for col in df.columns:
    if len(df[col].unique()) == 1:
        df.drop(col,inplace=True,axis=1)
print("Drop Done")
print("After dropping columns that only have one unique value")
print("------------------------------------------------------")
print("Number of records:" , len(df))
print("Number of features:" , len(df.columns), "\n")

print("===========================================================================")
print("Pre-processing Stage 2: Filling 0 to the missing values in leftover columns")
print("===========================================================================")
print("Replacing...")
columns_nan_value = df.loc[:, df.isna().sum() > 0].columns
for i, col in enumerate(columns_nan_value):
    df[col].fillna(value = 0, inplace=True)
print("Replace Done\n")

# Recheck for any blank columns
print("Recheck for any blank values in columns")
print("---------------------------------------")
print(df.isna().sum() > 0)

print("\n============================================================================")
print("Pre-processing Stage 3: Parse AUTHTXN_TRXN_DATE and AUTHTXN_TRXN_TIME column")
print("============================================================================")
'''
Convert date and time column to readable DateTime object
First, convert their types into String, then pre-process them as follows:
- For Dates which dont have year, assume it as 2018
- For Time(24-hour) which dont have Hours and Minutes infront, append the time with 00 : 00 : xx
Because of Excel automatically delete initial 0s infront,
'''
# Cast time and date as string first
df.AUTHTXN_TRXN_DATE = df.AUTHTXN_TRXN_DATE.astype(str)
df.AUTHTXN_TRXN_TIME = df.AUTHTXN_TRXN_TIME.astype(str)

print("Cleaning Date...")
for i, date in enumerate(df.AUTHTXN_TRXN_DATE):
    if len(date) == 3:
        new_date = pd.datetime.strptime(date, '%m%d').date()
        new_date = new_date.replace(2018, 1)
        df.at[i, 'AUTHTXN_TRXN_DATE'] = new_date
    elif len(date) == 4:
        new_date = pd.datetime.strptime(date, '%m%d').date()
        new_date = new_date.replace(2018)
        df.at[i, 'AUTHTXN_TRXN_DATE'] = new_date
    elif len(date) == 8:
        new_date = pd.datetime.strptime(date, '%Y%m%d').date()
        df.at[i, 'AUTHTXN_TRXN_DATE'] = new_date
print("Done")

print("\nCleaning Time...")
for i, time in enumerate(df.AUTHTXN_TRXN_TIME):
    if len(time) == 1 or len(time) == 2:
        new_time = pd.datetime.strptime(time, '%S').time()
        df.at[i, 'AUTHTXN_TRXN_TIME'] = new_time
    elif len(time) == 3:
        time = str(0) + time
        new_time = pd.datetime.strptime(time, '%M%S').time()
        df.at[i, 'AUTHTXN_TRXN_TIME'] = new_time
    elif len(time) == 4:
        new_time = pd.datetime.strptime(time, '%M%S').time()
        df.at[i, 'AUTHTXN_TRXN_TIME'] = new_time
    elif len(time) == 5:
        time = str(0) + time
        new_time = pd.datetime.strptime(time, '%H%M%S').time()
        df.at[i, 'AUTHTXN_TRXN_TIME'] = new_time
    elif len(time) == 6:
        new_time = pd.datetime.strptime(time, '%H%M%S').time()
        df.at[i, 'AUTHTXN_TRXN_TIME'] = new_time
print("Done\n")

print("Plotting number of transaction occures in 24 hours")
pd.to_datetime(df.AUTHTXN_TRXN_TIME, format='%H:%M:%S').dt.hour.value_counts().sort_index().plot()
plt.show()

print("\nExtracting Year, Month, Day, Day of week, Hour and Period of time from AUTHTXN_TRXN_DATE and AUTHTXN_TRXN_TIME column...")
import numpy as np
df["Year"] = pd.to_datetime(df.AUTHTXN_TRXN_DATE).dt.year
df["Month"] = pd.to_datetime(df.AUTHTXN_TRXN_DATE).dt.month
df["Day"] = pd.to_datetime(df.AUTHTXN_TRXN_DATE).dt.day
df["Day_of_week"] = pd.to_datetime(df.AUTHTXN_TRXN_DATE).dt.dayofweek

df["Hour"] = pd.to_datetime(df.AUTHTXN_TRXN_TIME, format='%H:%M:%S').dt.hour

# Period of time labeling
hours = df['Hour']
bins = [-1, 4, 8, 16, 20]
labels = ['Midnight', 'Morning','Afternoon','Evening','Night']
df['Period_of_time']  = np.array(labels)[np.array(bins).searchsorted(hours)-1]
print("Extract Done")

print("\nDropping AUTHTXN_TRXN_DATE and AUTHTXN_TRXN_TIME...")
df.drop(['AUTHTXN_TRXN_DATE'], axis=1, inplace=True)
df.drop(['AUTHTXN_TRXN_TIME'], axis=1, inplace=True)
print("Drop Done\n")


# Put AUTHTXN_FRAUD_CHECK as last column for easier to see
print("Put AUTHTXN_FRAUD_CHECK as last column for easier to see")
print("And map the AUTHTXN_FRAUD_CHECK (D as 1, F as 0)")
print("==================================================================")
df['AUTHTXN_FRAUD_CHECK'] = df['AUTHTXN_FRAUD_CHECK'].map({'F': 0, 'D': 1})
fraud_label_df = df.AUTHTXN_FRAUD_CHECK
df.drop(['AUTHTXN_FRAUD_CHECK'], axis=1, inplace=True)
df['AUTHTXN_FRAUD_CHECK'] = fraud_label_df
print("Done")


# Export the new dataset
print("Exporting the pre-processed dataset and fraud set(AUTHTXN_FRAUD_CHECK as D only)")
df.to_csv(outFilePath+'preprocessed_dataset.csv', index=False)
df.loc[df['AUTHTXN_FRAUD_CHECK'] == 'D'].to_excel(outFilePath+'fraud_set.xlsx', index=False)
print("Done export pre-processed dataset")

print("The preprocess file is outputed: " + outFilePath + "/preprocessed_dataset.csv")
print("The fraud set is output at: " + outFilePath + "/fraud_set.csv")

print("==================== End Pre-processing ====================")
