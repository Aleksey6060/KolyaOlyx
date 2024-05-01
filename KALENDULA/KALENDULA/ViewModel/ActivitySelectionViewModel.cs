using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Newtonsoft.Json;
using System.Windows.Input;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows;


namespace KALENDULA.ViewModel
{
    public class ActivitySelectionViewModel : INotifyPropertyChanged
    {
        private DateTime selectedDate;
        private string activityName;
        private Window window;
        

        private string LoadActivityForDay(DateTime date)
        {
            string jsonFilePath = "activities.json";
            if (File.Exists(jsonFilePath))
            {
                string jsonString = File.ReadAllText(jsonFilePath);
                List<MainWindowViewModel.Activity> activities = JsonConvert.DeserializeObject<List<MainWindowViewModel.Activity>>(jsonString);

                // Найти активность для выбранной даты
                MainWindowViewModel.Activity activity = activities.FirstOrDefault(a => a.Date.Date == date.Date);
                if (activity != null)
                {
                    return activity.Name;
                }
            }
            return string.Empty; // Или любое значение по умолчанию, если активности для даты нет
        }
        public ActivitySelectionViewModel(DateTime selectedDate, Window window)
        {
            this.selectedDate = selectedDate;
            this.activityName = LoadActivityForDay(selectedDate);
            this.window = window;
            SaveCommand = new RelayCommand(SaveActivity);
            CancelCommand = new RelayCommand(CancelActivity);
        }

        public string ActivityName
        {
            get => activityName;
            set
            {
                activityName = value;
                OnPropertyChanged();
            }
        }

        public ICommand SaveCommand { get; private set; }
        public ICommand CancelCommand { get; private set; }

        private void SaveActivity(object parameter)
        {
            // Сохранение активности в JSON-файл
            SaveActivityToJson();
            // Закрытие окна после сохранения
            CloseWindow();
        }

        private void CancelActivity(object parameter)
        {
            // Закрытие окна без сохранения
            CloseWindow();
        }

        private void SaveActivityToJson()
        {
            string jsonFilePath = "activities.json";
            List<MainWindowViewModel.Activity> activities;

            if (File.Exists(jsonFilePath))
            {
                string jsonString = File.ReadAllText(jsonFilePath);
                activities = JsonConvert.DeserializeObject<List<MainWindowViewModel.Activity>>(jsonString);
            }
            else
            {
                activities = new List<MainWindowViewModel.Activity>();
            }

            // Удаляем старую активность, если она есть, и добавляем новую
            activities.RemoveAll(activity => activity.Date.Date == selectedDate.Date);
            activities.Add(new MainWindowViewModel.Activity { Date = selectedDate, Name = ActivityName });

            string updatedJsonString = JsonConvert.SerializeObject(activities, Formatting.Indented);
            File.WriteAllText(jsonFilePath, updatedJsonString);
        }

        private void CloseWindow()
        {
            if (window != null)
            {
                window.Close();
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}