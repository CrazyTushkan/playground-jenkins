#! /usr/bin/python
import argparse
import itertools
import json
import requests
from datetime import datetime
from getpass import getpass
from tabulate import tabulate


SUCCESSFUL_CODES = [200]


def group_items(list_of_dicts, group_by):
    grouped_items = {}

    for item in list_of_dicts:
        grouped_items.setdefault(item[group_by], []).append(item)

    return grouped_items


def format_table(rows, headers):
    row_fmt = []
    formatted_rows = []
    total_width = 0

    for column_name in headers.keys():
        width = max([len(row[column_name]) for row in rows] + [len(column_name)])
        total_width += width
        row_fmt.append('{{:<{}}}'.format(width))

    row_fmt = "    ".join(row_fmt)
    total_width += (len(headers.keys()) - 1)*4
    formatted_rows.append(row_fmt.format(*headers.values()))
    formatted_rows.append('-'*total_width)
    formatted_rows.append('')

    for row in rows:
        row = [row[column_name] for column_name in headers.keys()]
        formatted_rows.append(row_fmt.format(*row))

    return '\n'.join(formatted_rows)


# TODO: Make this decorator
def validate_argument(argument, valid_values):
    if argument and argument not in valid_values:
        raise Exception('Invalid value {} for argument.\nValid values are: {}'.format(
                argument, valid_values))


def filter_fields(dictionary, fields):
    for key in list(dictionary.keys()):
        if key not in fields:
            del dictionary[key]


class Jenkins:

    def __init__(self, base_url, user, password):
        self.base_url = base_url
        self.user = user
        self.instance = requests.Session()
        self.instance.auth = (user, password)


    @staticmethod
    def _get_build_duration(timestamp, human=True):
        now = datetime.now()
        timestamp = datetime.fromtimestamp(timestamp / 1000)
        
        if human:
            delta = now - timestamp
            delta_seconds = delta.total_seconds()
            duration = {
                'days': delta.days,
                'hours': int(delta_seconds // 3600),
                'minutes': int(delta_seconds % 3600 // 60),
                'seconds': int(delta_seconds % 3600 % 60)
            }
            duration = ', '.join(
                ['{} {}'.format(v, k) for k, v in duration.items() if v > 0]
            )
        else:
            duration = (now - timestamp).total_seconds()

        return duration


    @staticmethod
    def _get_job_name(url):
        return url.split('/')[-3]


    @staticmethod
    def _get_node_labels(node):
        return [label['name'] for label in node['assignedLabels']]


    def _get_node_url(self, node):
        node_name = node['displayName']

        if node_name  == 'master':
            node_name = '(master)'

        return '{}/computer/{}/'.format(self.base_url, node_name)

    
    def base_api_request(self, request_args=''):
        return self.base_request('api/json?{}'.format(request_args))


    def base_computer_request(self, node_name='', request_args=''):
        return self.base_request('computer/{}/api/json?{}'.format(node_name, request_args))


    def base_queue_request(self, item_url='', request_args=''):
        if item_url:
            item_url = 'item/{}'.format(item_url.split('/')[-2])

        return self.base_request('queue/{}/api/json?{}'.format(item_url, request_args))


    def base_request(self, endpoint):
        results = self.instance.get('{}/{}'.format(self.base_url, endpoint))

        if results.status_code not in SUCCESSFUL_CODES:
            raise Exception('Got wrong status code: {}'.format(results.status_code))

        return results.json()


    def get_jobs(self):
        jobs = self.base_api_request('tree=jobs[*]')
        return jobs


    def get_executors(self, node_names=None, group_by=None):
        validate_argument(group_by, ['nodeName'])

        def _filter(item):
            result = True

            if node_names:
                result = item['nodeName'] in node_names

            return result

        executors = []
        request_args = 'tree=computer[displayName,executors[*[*]]]'
        result = self.base_computer_request(request_args=request_args)['computer']

        for computer in result:
            for executor in computer['executors']:
                executor['nodeName'] = computer['displayName']
                executors.append(executor)

        executors = list(filter(_filter, executors))

        if group_by:
            executors = group_items(executors, group_by)

        return executors


    def get_nodes(self, node_names=None, labels=None):
        
        def _filter(item):
            result = True
            
            if node_names:
                result = item['displayName'] in node_names

            if labels:
                result = all(label in item['assignedLabels'] for label in labels)

            return result

        nodes = self.base_computer_request()['computer']

        for node in nodes:
            node.update({
                'assignedLabels': self._get_node_labels(node),
                'url': self._get_node_url(node),
            })

        nodes = list(filter(_filter, nodes))

        return nodes

    
    def get_running_builds(self, group_by=None, job_names=None, node_names=None, human_timestamp=True, fields=None, sort_by=None):
        validate_argument(group_by, ['nodeName', 'jobName'])

        def _filter(item):
            result = True

            if job_names:
                result = item['jobName'] in job_names

            return result

        builds = []
        executors = self.get_executors(node_names=node_names)
        
        for executor in executors:
            build = executor['currentExecutable']
            build.update({
                'duration': self._get_build_duration(build['timestamp'], human=human_timestamp),
                'jobName': self._get_job_name(build['url']),
                'nodeName': executor['nodeName'],
            })
            
            if fields:
                filter_fields(build, fields)

            builds.append(build)

        builds = list(filter(_filter, builds))

        if sort_by:
            builds = sorted(builds, key=lambda item: item[sort_by])

        if  group_by:
            builds = group_items(builds, group_by)

        return builds


    def get_queue(self, group_by=None, job_names=None, human_timestamp=True):
        validate_argument(group_by, ['jobName', 'why'])

        def _filter(item):
            result = True

            if job_names:
                result = item['jobName'] in job_names

            return result

        queue = self.base_queue_request()['items']

        for build in queue:
            build.update({
                'timeInQueue': self._get_build_duration(build['inQueueSince'], human=human_timestamp),
            })

        queue = list(filter(_filter, queue))

        if group_by:
            queue = group_items(queue, group_by)

        return queue


def get_builds(server, args):
    builds = server.get_running_builds(
        fields=['url', 'nodeName', 'jobName', 'duration', 'timestamp'],
        group_by=args.group_by,
        sort_by=args.sort_by,
        node_names=args.node_names,
        job_names=args.job_names)
    
    headers = {
        'jobName': 'Job name',
        'url': 'URL',
        'nodeName': 'Node name',
        'duration': 'Duration',
    }

    if args.group_by:
        for group_name, items in builds.items():
            print('Jobs for: {}'.format(group_name))
            print(format_table(items, headers), '\n')
    else:
        print(format_table(builds, headers))


def main():
    commands = {
        'get_builds': get_builds,
    }
        
    parser = argparse.ArgumentParser(description='CLI tool for querying info from Jenkins')
    parser.add_argument(
        '-u',
        dest='user',
        help='Username'
    )
    parser.add_argument(
        '-p',
        dest='password',
        help='Password'
    )

    required_args = parser.add_argument_group('required arguments')
    required_args.add_argument(
        '-H',
        dest='host',
        required=True,
        help='Jenkins host URL'
    )

    subparsers = parser.add_subparsers()

    getbuilds_parser = subparsers.add_parser(
        'get_builds',
        help='Get running builds on Jenkins')
    getbuilds_parser.add_argument(
        '--sort',
        choices=['timestamp', 'jobName', 'nodeName'],
        default='timestamp',
        dest='sort_by',
        help='Sort builds by field')
    getbuilds_parser.add_argument(
        '--group',
        choices=['jobName', 'nodeName'],
        default=None,
        dest='group_by',
        help='Filter builds by job names')
    getbuilds_parser.add_argument(
        '--nodes',
        dest='node_names',
        nargs='+',
        help='Filter builds by node names')
    getbuilds_parser.add_argument(
        '--jobs',
        dest='job_names',
        nargs='+',
        help='Filter builds by job names')
    getbuilds_parser.set_defaults(
        func=get_builds)

    args = parser.parse_args()

    if not args.user:
        args.user = input('Username: ')

    if not args.password:
        args.password = getpass('Password: ')

    server = Jenkins(args.host, args.user, args.password)

    args.func(server, args)
    

if __name__ == '__main__':
    main()
