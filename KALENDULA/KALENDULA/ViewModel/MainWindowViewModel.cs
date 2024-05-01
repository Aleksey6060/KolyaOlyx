using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Runtime.CompilerServices;
using System.Windows.Input;
using System.Windows;
using System.IO;
using Newtonsoft.Json;
using KALENDAR;

namespace KALENDULA.ViewModel
{
    public class MainWindowViewModel : INotifyPropertyChanged
    {
        public class Activity
        {
            public DateTime Date { get; set; }
            public string Name { get; set; }
        }

        private DateTime displayDate;

        public MainWindowViewModel()
        {
            DisplayDate = DateTime.Today;
            NavigateBackCommand = new RelayCommand(NavigateBack);
            NavigateForwardCommand = new RelayCommand(NavigateForward);
            OpenDayCommand = new RelayCommand(OpenDay);
            ClearDayCommand = new RelayCommand(ClearDay);
        }

        public DateTime DisplayDate
        {
            get => displayDate;
            set
            {
                displayDate = value;
                OnPropertyChanged();
            }
        }

        public ICommand NavigateBackCommand { get; private set; }
        public ICommand NavigateForwardCommand { get; private set; }
        public ICommand OpenDayCommand { get; private set; }
        public ICommand ClearDayCommand { get; private set; }

        private void NavigateBack(object parameter)
        {
            DisplayDate = DisplayDate.AddMonths(-1);
        }

        private void NavigateForward(object parameter)
        {
            DisplayDate = DisplayDate.AddMonths(1);
        }

        private void OpenDay(object parameter)
        {
            var selectedDate = (DateTime)parameter;
            var activitySelectionWindow = new ActivitySelectionWindow(selectedDate, this);
            activitySelectionWindow.ShowDialog();
        }

        private void ClearDay(object parameter)
        {
            // Очистка активности для выбранной даты
            var selectedDate = (DateTime)parameter;
            string jsonFilePath = "activities.json";
            if (File.Exists(jsonFilePath))
            {
                string jsonString = File.ReadAllText(jsonFilePath);
                List<Activity> activities = JsonConvert.DeserializeObject<List<Activity>>(jsonString);

                // Удаляем активность для выбранной даты
                activities.RemoveAll(activity => activity.Date.Date == selectedDate.Date);

                string updatedJsonString = JsonConvert.SerializeObject(activities, Formatting.Indented);
                File.WriteAllText(jsonFilePath, updatedJsonString);
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;

        protected virtual void OnPropertyChanged([CallerMemberName] string propertyName = null)
        {
            PropertyChanged?.Invoke(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}